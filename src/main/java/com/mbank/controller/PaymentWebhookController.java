package com.mbank.controller;

import com.mbank.service.PaymentService;
import com.mbank.util.WebhookUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment/webhook")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        try {
            // 🔐 1. Verify signature
            boolean isValid = WebhookUtil.verifyWebhookSignature(payload, signature, webhookSecret);

            if (!isValid) {
                log.error("Invalid webhook signature");
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");

            log.info("Webhook received: {}", event);

            // ✅ 2. Handle events
            if ("payment.captured".equals(event)) {

                JSONObject paymentEntity = json
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String paymentId = paymentEntity.getString("id");
                String orderId = paymentEntity.getString("order_id");

                // 🔥 Call existing verify logic
                paymentService.processWebhookPayment(orderId, paymentId);

            } else if ("payment.failed".equals(event)) {

                log.warn("Payment failed webhook received");

                // optional: mark order as FAILED
            }

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            log.error("Webhook error: ", e);
            return ResponseEntity.internalServerError().body("Error");
        }
    }
}