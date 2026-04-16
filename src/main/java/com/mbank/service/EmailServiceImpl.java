package com.mbank.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;




import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;


import jakarta.mail.MessagingException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public CompletableFuture<Void> sendEmail(String to, String subject, String text) {
        val future = new CompletableFuture<Void>();

        try {
            val message = mailSender.createMimeMessage();
            val helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            // From address is automatically set by Spring Boot based on your properties
            helper.setSubject(subject);
            helper.setText(text, true); // Set the second parameter to true to send HTML content
            mailSender.send(message);

            log.info("Sent email to {}", to);
            future.complete(null);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}", to, e);
            future.completeExceptionally(e);
        }

        return future;
    }

@Override
public String getLoginEmailTemplate(String name, String loginTime, String loginLocation) {

    return String.format("""
    <div style="margin:0;padding:0;background-color:#eef2f7;font-family:Arial,sans-serif;">
        
        <table width="100%%" cellpadding="0" cellspacing="0" style="padding:20px 0;">
            <tr>
                <td align="center">

                    <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 6px 20px rgba(0,0,0,0.08);">

                        <tr>
                            <td style="background:#0d47a1;color:#ffffff;padding:20px;text-align:center;">
                                <h2 style="margin:0;">M Bank</h2>
                                <p style="margin:5px 0 0;font-size:14px;">Trusted & Secure Banking</p>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333;">
                                <p style="font-size:16px;">Dear %s,</p>

                                <p style="font-size:14px;">
                                    We detected a login to your account. Please review the details below:
                                </p>

                                <table width="100%%" style="background:#f4f6f8;border-radius:8px;padding:15px;margin:20px 0;">
                                    <tr>
                                        <td><strong>Login Time:</strong></td>
                                        <td>%s</td>
                                    </tr>
                                    <tr>
                                        <td><strong>Location:</strong></td>
                                        <td>%s</td>
                                    </tr>
                                </table>

                                <p style="font-size:14px;">
                                    If this was you, you can safely ignore this message.
                                </p>

                                <p style="font-size:14px;color:#d32f2f;">
                                    <strong>If this was not you, please secure your account immediately.</strong>
                                </p>

                                <div style="text-align:center;margin:30px 0;">
                                    <a href="#" style="background:#0d47a1;color:#ffffff;padding:12px 25px;border-radius:5px;text-decoration:none;font-size:14px;">
                                        Secure My Account
                                    </a>
                                </div>

                                <p style="font-size:14px;">
                                    Regards,<br/>
                                    <strong>M Bank Security Team</strong>
                                </p>
                            </td>
                        </tr>

                        <tr>
                            <td style="background:#f4f6f8;padding:15px;text-align:center;font-size:12px;color:#777;">
                                <p>This is an automated email. Please do not reply.</p>
                                <p>© 2026 M Bank. All rights reserved.</p>
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </div>
    """, name, loginTime, loginLocation);
}
@Override
public String getOtpLoginEmailTemplate(String name, String accountNumber, String otp) {

    return String.format("""
    <div style="margin:0;padding:0;background-color:#eef2f7;font-family:Arial,sans-serif;">
        
        <table width="100%%" cellpadding="0" cellspacing="0" style="padding:20px 0;">
            <tr>
                <td align="center">

                    <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 6px 20px rgba(0,0,0,0.08);">

                        <tr>
                            <td style="background:#0d47a1;color:#ffffff;padding:20px;text-align:center;">
                                <h2 style="margin:0;">M Bank</h2>
                                <p style="margin:5px 0 0;font-size:14px;">OTP Verification</p>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333;">
                                <p style="font-size:16px;">Dear %s,</p>

                                <p style="font-size:14px;">
                                    Use the following One-Time Password (OTP) to complete your login:
                                </p>

                                <div style="text-align:center;margin:25px 0;">
                                    <span style="display:inline-block;font-size:26px;letter-spacing:5px;background:#0d47a1;color:#ffffff;padding:15px 30px;border-radius:8px;">
                                        %s
                                    </span>
                                </div>

                                <p style="font-size:14px;">
                                    <strong>Account Number:</strong> %s
                                </p>

                                <p style="font-size:14px;">
                                    This OTP is valid for <strong>%d minutes</strong>.
                                </p>

                                <p style="font-size:14px;color:#d32f2f;">
                                    <strong>Never share your OTP with anyone.</strong>
                                </p>

                                <div style="text-align:center;margin:30px 0;">
                                    <a href="#" style="background:#0d47a1;color:#ffffff;padding:12px 25px;border-radius:5px;text-decoration:none;font-size:14px;">
                                        Continue Secure Login
                                    </a>
                                </div>

                                <p style="font-size:14px;">
                                    Regards,<br/>
                                    <strong>M Bank Security Team</strong>
                                </p>
                            </td>
                        </tr>

                        <tr>
                            <td style="background:#f4f6f8;padding:15px;text-align:center;font-size:12px;color:#777;">
                                <p>This is an automated email. Please do not reply.</p>
                                <p>© 2026 M Bank. All rights reserved.</p>
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </div>
    """, name, otp, accountNumber, OtpServiceImpl.OTP_EXPIRY_MINUTES);
}
public String buildProfessionalEmail(String accountNumber) {

    return """
        <html>
        <body style="font-family: Arial; background-color:#f4f6f8; padding:20px;">
            <div style="max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;">
                
                <h2 style="color:#2c3e50;">M Bank</h2>
                
                <p>Dear Customer,</p>
                
                <p>Your bank statement for account <b>%s</b> is attached to this email.</p>
                
                <p><b>Security Note:</b> This PDF is password protected.</p>
                
                <p>Password format:</p>
                <ul>
                    <li>Your Account Number</li>
                </ul>

                <p>If you did not request this, please contact support immediately.</p>
                
                <br/>
                <p>Regards,<br/>M Bank Team</p>
            </div>
        </body>
        </html>
    """.formatted(accountNumber);
}
public void sendEmailWithAttachment(String to,
                                    String subject,
                                    String htmlContent,
                                    byte[] pdfData) {

    MimeMessage message = mailSender.createMimeMessage();

    try {
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        helper.addAttachment("Bank_Statement.pdf",
                new ByteArrayResource(pdfData));

        mailSender.send(message);

    } catch (Exception e) {
        throw new RuntimeException("Failed to send email", e);
    }
}
}
