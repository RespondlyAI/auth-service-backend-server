package in.respondlyai.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class VerifyOtpRequest {

    @NotBlank(message = "user_id is required")
    @JsonProperty("user_id")
    String userId

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    String otp

}
