package in.respondlyai.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    String email

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    String otp

}
