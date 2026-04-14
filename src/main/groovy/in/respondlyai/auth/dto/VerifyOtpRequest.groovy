package in.respondlyai.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "OTP verification request")
class VerifyOtpRequest {

    @Schema(description = "User's primary UUID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    @NotNull(message = "ID is required")
    UUID id

    @Schema(description = "6-digit OTP code", example = "123456")
    @NotBlank(message = "OTP is required")
    String otp

}
