package in.respondlyai.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Pattern

@io.swagger.v3.oas.annotations.media.Schema(description = "Login credentials")
class LoginRequest {
    @io.swagger.v3.oas.annotations.media.Schema(description = "User email", example = "johndoe@gmail.com")
    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "Email must be less than 50 characters")
    @Email
    @Pattern(regexp = '^[A-Za-z0-9._%+-]+@gmail\\.com$', message = "Only @gmail.com emails are allowed")
    String email

    @io.swagger.v3.oas.annotations.media.Schema(description = "User password", example = "Password123!")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    String password
}
