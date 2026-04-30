package com.mbank.service;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mbank.dto.CreateOrderRequest;
import com.mbank.dto.CreateOrderResponse;
import com.mbank.dto.VerifyPaymentRequest;
import com.mbank.entity.Account;
import com.mbank.entity.Payment;
import com.mbank.entity.PaymentOrder;
import com.mbank.entity.TransactionLedger;
import com.mbank.repository.AccountRepository;
import com.mbank.repository.PaymentOrderRepository;
import com.mbank.repository.PaymentRepository;
import com.mbank.repository.TransactionLedgerRepository;
import com.mbank.util.LoggedinUser;
import com.mbank.util.PaymentUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final TransactionLedgerRepository ledgerRepo;
    private final AccountService accountService;
    private final AccountRepository accountRepository; // ✅ UserRepository removed — no longer needed

    @Value("${razorpay.key}")
    private String key;

    @Value("${razorpay.secret}")
    private String secret;

    // ✅ Helper: resolve Account from the logged-in account number
    private Account getLoggedInAccount() {
        String accountNumber = LoggedinUser.getAccountNumber();
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        return account;
    }

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) throws Exception {

        // ✅ Resolve userId from logged-in account via OneToOne User relation
        Account account = getLoggedInAccount();
        Long userId = account.getUser().getId(); // ✅ FIX: Account has User object, not userId directly

        // ✅ 1. Validate amount
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        if (request.getAmount() > 1000000) {
            throw new RuntimeException("Amount exceeds limit");
        }

        // ✅ 2. Convert to paise safely
        long amountInPaise = Math.round(request.getAmount() * 100);

        RazorpayClient client = new RazorpayClient(key, secret);

        // ✅ 3. Create unique receipt
        String receipt = "txn_" + System.currentTimeMillis();

        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", receipt);

        JSONObject notes = new JSONObject();
        notes.put("userId", userId);
        options.put("notes", notes);

        Order order = client.orders.create(options);

        // ✅ 4. Save order with userId correctly set (was missing before!)
        PaymentOrder paymentOrder = PaymentOrder.builder()
                .orderId(order.get("id"))
                .userId(userId)           // ✅ FIX: userId was missing in your previous version
                .amount(request.getAmount())
                .currency("INR")
                .status("CREATED")
                .receipt(receipt)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepo.save(paymentOrder);

        // ✅ 5. Return response for frontend
        return CreateOrderResponse.builder()
                .orderId(order.get("id"))
                .amount(request.getAmount())
                .currency("INR")
                .key(key)
                .build();
    }

    @Override
    @Transactional
    public String verifyPayment(VerifyPaymentRequest req) { // ✅ FIX: removed unsafe Long userId param

        // ✅ Resolve userId server-side from logged-in session (never trust frontend)
        Account account = getLoggedInAccount();
        Long userId = account.getUser().getId(); // ✅ FIX: navigate OneToOne relation to get User id

        // ✅ 1. Idempotency check
        if (paymentRepo.existsByPaymentId(req.getPaymentId())) {
            return "Already Processed";
        }

        // ✅ 2. Fetch Order
        PaymentOrder order = orderRepo.findByOrderId(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ✅ 3. Validate ownership
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized payment access");
        }

        // ✅ 4. Prevent duplicate order processing
        if ("PAID".equalsIgnoreCase(order.getStatus())) {
            return "Order already paid";
        }

        // ✅ 5. Signature Verification (CRITICAL)
        boolean isValid = PaymentUtil.verifySignature(
                req.getOrderId(),
                req.getPaymentId(),
                req.getSignature(),
                secret
        );

        if (!isValid) {
            throw new RuntimeException("Invalid payment signature");
        }

        // ✅ 6. Save Payment
        Payment payment = Payment.builder()
                .paymentId(req.getPaymentId())
                .orderId(req.getOrderId())
                .userId(userId)
                .amount(order.getAmount())
                .method("ONLINE")
                .status("SUCCESS")
                .signature(req.getSignature())
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepo.save(payment);

        // ✅ 7. Update Order status
        order.setStatus("PAID");
        orderRepo.save(order);

        // ✅ 8. Ledger Entry
        ledgerRepo.save(TransactionLedger.builder()
                .userId(userId)
                .type("CREDIT")
                .amount(order.getAmount())
                .referenceId(req.getPaymentId())
                .description("Payment Gateway Credit")
                .createdAt(LocalDateTime.now())
                .build());

        // ✅ 9. Credit Account
        accountService.creditFromPayment(userId, order.getAmount(), req.getPaymentId());

        return "SUCCESS";
    }

    @Override
    public String getPaymentStatus(String orderId) {
        return orderRepo.findByOrderId(orderId)
                .map(PaymentOrder::getStatus)
                .orElse("NOT_FOUND");
    }

    @Override
    @Transactional
    public void processWebhookPayment(String orderId, String paymentId) {

        // ✅ Idempotency check
        if (paymentRepo.existsByPaymentId(paymentId)) {
            return;
        }

        PaymentOrder order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Long userId = order.getUserId(); // ✅ Webhook: userId comes from saved order (no session)

        // Save payment
        paymentRepo.save(Payment.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(order.getAmount())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build());

        // Update order
        order.setStatus("PAID");
        orderRepo.save(order);

        // Ledger
        ledgerRepo.save(TransactionLedger.builder()
                .userId(userId)
                .type("CREDIT")
                .amount(order.getAmount())
                .referenceId(paymentId)
                .description("Webhook Payment Credit")
                .createdAt(LocalDateTime.now())
                .build());

        // Credit account
        accountService.creditFromPayment(userId, order.getAmount(), paymentId);
    }
}