package com.lms.dto;

/**
 * Returned by UserService.createPasswordResetToken to the servlet, which
 * builds the reset link and hands it to util.MailSender.
 * Carries the RAW token (never persisted - only its SHA-256 hash is stored).
 */
public class PasswordResetRequestDTO {

    private String rawToken;
    private String recipientEmail;
    private String recipientName;

    public String getRawToken() {
        return rawToken;
    }

    public void setRawToken(String rawToken) {
        this.rawToken = rawToken;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
