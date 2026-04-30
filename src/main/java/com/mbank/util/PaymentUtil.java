package com.mbank.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class PaymentUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    // 🔐 Generate HMAC SHA256 Signature
    public static String hmacSHA256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex
            StringBuilder hex = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC : " + e.getMessage());
        }
    }

    // ✅ Verify Razorpay Signature
    public static boolean verifySignature(String orderId,
                                          String paymentId,
                                          String signature,
                                          String secret) {

        String payload = orderId + "|" + paymentId;

        String generatedSignature = hmacSHA256(payload, secret);

        return generatedSignature.equals(signature);
    }
}