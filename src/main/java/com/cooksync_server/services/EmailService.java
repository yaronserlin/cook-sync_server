package com.cooksync_server.services;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for sending transactional emails to users.
 *
 * <p><strong>Stub implementation:</strong> no SMTP/mail provider is configured yet, so this
 * currently only logs the outgoing message instead of delivering it. Once mail provider
 * credentials are available, replace the body of {@link #sendPasswordResetEmail} with a real
 * {@code JavaMailSender} (or equivalent) call — the method signature and call sites in
 * {@link AuthService} are already the intended long-term contract, so no other code needs to
 * change.</p>
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 05/08/2026
 */
@Slf4j
@Service
public class EmailService implements IEmailService{

    /**
     * Sends a password-reset email containing the given token to the given address.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param toEmail the recipient's email address
     * @param resetToken the one-time password-reset token to include in the email
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        log.info("[STUB] Password reset requested for {} — token: {} (no mail provider configured, email not actually sent)",
                toEmail, resetToken);
    }
}
