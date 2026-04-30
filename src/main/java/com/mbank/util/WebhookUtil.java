package com.mbank.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class WebhookUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static boolean verifyWebhookSignature(String payload, String signature, String secret) {

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : rawHmac) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }

            String generatedSignature = hex.toString();

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            throw new RuntimeException("Webhook signature verification failed");
        }
    }
}
