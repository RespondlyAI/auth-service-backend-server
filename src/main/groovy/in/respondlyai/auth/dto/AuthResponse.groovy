package in.respondlyai.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Authentication response")
class AuthResponse {
    @Schema(description = "JWT access token")
    String token

    @Schema(description = "JWT refresh token")
    String refreshToken

    @Schema(description = "Token type", example = "Bearer")
    String type = "Bearer"

    @Schema(description = "User ID")
    @JsonProperty("user_id")
    UUID id

    @Schema(description = "User email")
    String email

    @Schema(description = "User role")
    String role

    AuthResponse(String token, String refreshToken, UUID id, String email, String role) {
        this.token = token
        this.refreshToken = refreshToken
        this.id = id
        this.email = email
        this.role = role
    }
}
