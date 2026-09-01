package util;

/**
 * Abstraction over the email provider so BillingController can be unit
 * tested with a mock instead of making real HTTP calls.
 */
public interface EmailService {

    void sendBillEmail(String toEmail, String toName, String subject, String receiptText) throws EmailException;
}
