package com.lms.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends the password-reset email over SMTP.
 * If SMTP credentials are left blank in app.properties, the reset link is
 * written to the Tomcat console instead of failing, so the feature is
 * testable end-to-end with zero credentials.
 */
public class MailSender {

    private static final Logger LOGGER = Logger.getLogger(MailSender.class.getName());

    public void sendPasswordReset(String recipientEmail, String recipientName, String resetLink) {
        if (!AppConfig.isConfigured("smtp.host", "smtp.username", "smtp.password")) {
            LOGGER.info(() -> "[MailSender] SMTP not configured - password reset link for "
                    + recipientEmail + ": " + resetLink);
            return;
        }

        String host = AppConfig.get("smtp.host");
        int port = AppConfig.getInt("smtp.port", 587);
        String username = AppConfig.get("smtp.username");
        String password = AppConfig.get("smtp.password");
        String from = AppConfig.get("smtp.from", username);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Đặt lại mật khẩu LMS");
            String greeting = (recipientName == null || recipientName.isBlank()) ? "" : recipientName + ",\n\n";
            message.setText(greeting
                    + "Bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu cho tài khoản LMS của bạn.\n"
                    + "Nhấn vào liên kết dưới đây để đặt mật khẩu mới (liên kết có hiệu lực trong thời gian giới hạn):\n\n"
                    + resetLink + "\n\n"
                    + "Nếu bạn không yêu cầu điều này, bạn có thể bỏ qua email này.");
            Transport.send(message);
        } catch (MessagingException e) {
            // Don't let a mail-server hiccup surface as a 500 to the user; the
            // servlet always shows the same neutral "check your email" message,
            // and this failure is fully logged for an admin to investigate.
            LOGGER.log(Level.SEVERE, "Failed to send password reset email to " + recipientEmail, e);
        }
    }

    /**
     * Sends the registration-confirmation email. The account does not exist
     * yet at this point - it is only created once this link is clicked (see
     * UserService.confirmRegistration()).
     * If SMTP credentials are left blank in app.properties, the confirmation
     * link is written to the Tomcat console instead of failing, so the
     * feature is testable end-to-end with zero credentials.
     */
    public void sendRegistrationConfirmation(String recipientEmail, String recipientName, String confirmLink) {
        if (!AppConfig.isConfigured("smtp.host", "smtp.username", "smtp.password")) {
            LOGGER.info(() -> "[MailSender] SMTP not configured - registration confirmation link for "
                    + recipientEmail + ": " + confirmLink);
            return;
        }

        String host = AppConfig.get("smtp.host");
        int port = AppConfig.getInt("smtp.port", 587);
        String username = AppConfig.get("smtp.username");
        String password = AppConfig.get("smtp.password");
        String from = AppConfig.get("smtp.from", username);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Xác nhận đăng ký tài khoản LMS");
            String greeting = (recipientName == null || recipientName.isBlank()) ? "" : recipientName + ",\n\n";
            message.setText(greeting
                    + "Cảm ơn bạn đã đăng ký tài khoản trên hệ thống LMS.\n"
                    + "Nhấn vào liên kết dưới đây để xác nhận email và kích hoạt tài khoản của bạn " +
                    "(liên kết có hiệu lực trong thời gian giới hạn):\n\n"
                    + confirmLink + "\n\n"
                    + "Nếu bạn không thực hiện yêu cầu đăng ký này, bạn có thể bỏ qua email này.");
            Transport.send(message);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send registration confirmation email to " + recipientEmail, e);
        }
    }
}
