package in.respondlyai.auth.service

import groovy.util.logging.Slf4j
import in.respondlyai.auth.dto.EmailRequest
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Slf4j
@Service
class EmailService {

    @Value('${email-service.url}')
    private String apiUrl

    @Value('${email-service.from}')
    private String apiFrom

    private final RestTemplate restTemplate = new RestTemplate()

    void sendWelcomeEmail(String toEmail, String name) {
        sendEmail(
            "welcome_owner",
            toEmail,
            "Welcome to RespondlyAI!",
            [name: name]
        )
    }

    void sendOtpEmail(String toEmail, String otp) {
        sendEmail(
            "otp_signup",
            toEmail,
            "Your RespondlyAI Verification Code",
            [otp: otp]
        )
    }

    void sendPasswordResetEmail(String toEmail, String name, String link) {
        sendEmail(
            "password_reset",
            toEmail,
            "Reset your RespondlyAI password",
            [name: name, link: link]
        )
    }

    private void sendEmail(String eventType, String toEmail, String subject, Map<String, Object> metadata) {
        try {
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)

            EmailRequest request = new EmailRequest(
                event_type: eventType,
                event_source: "auth-service",
                idempotency_key: UUID.randomUUID().toString(),
                correlation_id: UUID.randomUUID().toString(),
                email: new EmailRequest.EmailDetails(
                    from: apiFrom,
                    to: [toEmail],
                    subject: subject
                ),
                metadata: metadata
            )

            HttpEntity<EmailRequest> entity = new HttpEntity<>(request, headers)

            log.info("Sending {} email via Email Service to: {}", eventType, toEmail)
            
            def response = restTemplate.exchange(apiUrl + "/emails/send", HttpMethod.POST, entity, Map.class)
            
            log.info("Email Service response: {}", response.body)
        } catch (Exception e) {
            log.error("Failed to send {} email to {}: {}", eventType, toEmail, e.getMessage(), e)
        }
    }
}
