package in.respondlyai.auth.exception

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Structured error response DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Structured error response")
class ApiErrorResponse {

    @Schema(description = "Always false for errors", example = "false")
    boolean success = false

    @Schema(description = "Human-readable error message", example = "Email already in use")
    String message

    @Schema(description = "Error type category", example = "CONFLICT")
    ErrorType type

    @Schema(description = "ISO 8601 timestamp", example = "2026-02-21T14:30:00.000Z")
    String timestamp

    @Schema(description = "Stack trace (dev mode only)", hidden = true)
    String stack

    @Schema(description = "Additional error context (dev mode only)")
    Map<String, Object> metadata

    @Schema(description = "Request path (dev mode only)", example = "/api/auth/signup")
    String path

    @Schema(description = "HTTP method (dev mode only)", example = "POST")
    String method

    ApiErrorResponse(String message, ErrorType type) {
        this.message = message
        this.type = type    
        this.timestamp = Instant.now().toString()
    }
}
