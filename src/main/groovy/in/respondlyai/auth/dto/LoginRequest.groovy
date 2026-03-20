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
    @Pattern(regexp = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$', message = "enter valid mail id only")
    String email

    @io.swagger.v3.oas.annotations.media.Schema(description = "User password", example = "Password123!")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    @Pattern(regexp= '^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$' , message="enter valid password only")
    String password
}
