package in.respondlyai.auth.dto

import groovy.transform.Canonical

@Canonical
class EmailRequest {
    String event_type
    String event_source = "auth-service"
    String idempotency_key
    String correlation_id
    EmailDetails email
    Map<String, Object> metadata

    @Canonical
    static class EmailDetails {
        String from
        List<String> to
        String subject
    }
}
