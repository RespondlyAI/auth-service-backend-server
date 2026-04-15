package in.respondlyai.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Signup request")
class SignupRequest {

    @Schema(description = "User's full name", example = "John Doe")
    @NotBlank(message = "Name is required")
    String name

    @Schema(description = "User's email (Gmail only)", example = "johndoe@gmail.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(regexp = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$')
    String email

    @Schema(description = "User's password", example = "password123")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    String password

    @Schema(description = "User's role (defaults to OWNER for signup)", example = "OWNER")
    String role = "OWNER"
}