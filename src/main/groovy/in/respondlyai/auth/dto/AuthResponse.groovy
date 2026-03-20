package in.respondlyai.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import in.respondlyai.auth.entity.Role
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Authentication response")
class AuthResponse {
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token

    @Schema(description = "Token type", example = "Bearer")
    String type = "Bearer"

    @Schema(description = "User ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    @JsonProperty("user_id")
    String id

    @Schema(description = "User email", example = "johndoe@gmail.com")
    String email

    @Schema(description = "User role", example = "OWNER")
    Role role

    AuthResponse(String token, String id, String email, Role role) {
        this.token = token
        this.id = id
        this.email = email
        this.role = role
    }
}
