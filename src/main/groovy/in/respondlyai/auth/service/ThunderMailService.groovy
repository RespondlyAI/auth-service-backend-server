package in.respondlyai.auth.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ThunderMailService {

    private static final Logger log = LoggerFactory.getLogger(ThunderMailService)

    @Value('${thundermail.api.url}')
    private String apiUrl

    @Value('${thundermail.api.token}')
    private String apiToken

    @Value('${thundermail.api.from}')
    private String apiFrom

    private final RestTemplate restTemplate = new RestTemplate()

    void sendWelcomeEmail(String toEmail, String name) {
        try {
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.setBearerAuth(apiToken)

            Map<String, Object> body = [
                from: apiFrom,
                to: toEmail,
                subject: "Welcome to RespondlyAI!",
                html: "<strong>Hi " + name + ", welcome to RespondlyAI!</strong>"
            ]

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers)

            log.info("Sending welcome email via ThunderMail to: {}", toEmail)
            
            def response = restTemplate.exchange(apiUrl + "/emails", HttpMethod.POST, entity, Map.class)
            
            log.info("ThunderMail response: {}", response.body)
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e)
        }
    }
    void sendOtpEmail(String toEmail, String otp) {
        try {
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_JSON)
            headers.setBearerAuth(apiToken)

            Map<String, Object> body = [
                from: apiFrom,
                to: toEmail,
                subject: "Your RespondlyAI Verification Code",
                html: "<h2>Your verification code is: " + otp + "</h2><p>It expires in 5 minutes.</p>"
            ]

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers)

            log.info("Sending OTP email via ThunderMail to: {}", toEmail)
            
            def response = restTemplate.exchange(apiUrl + "/emails", HttpMethod.POST, entity, Map.class)
            
            log.info("ThunderMail response for OTP: {}", response.body)
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e)
        }
    }
}
