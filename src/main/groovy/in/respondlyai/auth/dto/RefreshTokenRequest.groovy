package in.respondlyai.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Token request (for refresh/logout)")
class RefreshTokenRequest {

    @Schema(description = "JWT refresh token")
    @NotBlank(message = "Refresh token is required")
    String refreshToken

}
