package util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends transactional email through the Brevo (formerly Sendinblue) REST
 * API. Credentials are read from environment variables rather than hardcoded
 * so the API key never ends up committed to source control:
 *   BREVO_API_KEY      - required, generated in Brevo under SMTP & API
 *   BREVO_SENDER_EMAIL - required, must be a sender verified in Brevo
 *   BREVO_SENDER_NAME  - optional, defaults to "Sunrise Dental Clinic"
 */
public final class BrevoEmailService implements EmailService {

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String DEFAULT_SENDER_NAME = "Sunrise Dental Clinic";

    private final HttpClient httpClient;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;

    public BrevoEmailService() {
        this(System.getenv("BREVO_API_KEY"), System.getenv("BREVO_SENDER_EMAIL"),
                System.getenv("BREVO_SENDER_NAME"));
    }

    public BrevoEmailService(String apiKey, String senderEmail, String senderName) {
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = (senderName == null || senderName.isBlank()) ? DEFAULT_SENDER_NAME : senderName;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void sendBillEmail(String toEmail, String toName, String subject, String receiptText)
            throws EmailException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new EmailException("Brevo API key is not configured. Set the BREVO_API_KEY environment variable.");
        }
        if (senderEmail == null || senderEmail.isBlank()) {
            throw new EmailException(
                    "Brevo sender email is not configured. Set the BREVO_SENDER_EMAIL environment variable.");
        }

        String htmlContent = "<pre style=\"font-family:monospace;\">" + escapeHtml(receiptText) + "</pre>";
        String json = "{"
                + "\"sender\":{\"name\":\"" + escapeJson(senderName) + "\",\"email\":\"" + escapeJson(senderEmail) + "\"},"
                + "\"to\":[{\"email\":\"" + escapeJson(toEmail) + "\",\"name\":\"" + escapeJson(toName) + "\"}],"
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"htmlContent\":\"" + escapeJson(htmlContent) + "\","
                + "\"textContent\":\"" + escapeJson(receiptText) + "\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new EmailException("Brevo API returned status " + response.statusCode()
                        + ": " + response.body());
            }
        } catch (IOException e) {
            throw new EmailException("Failed to send email: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailException("Email sending was interrupted: " + e.getMessage());
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
