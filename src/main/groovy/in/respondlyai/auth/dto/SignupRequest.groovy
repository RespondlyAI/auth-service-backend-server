package in.respondlyai.auth.dto

import in.respondlyai.auth.entity.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Signup request payload")
class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
    @Schema(description = "User's full name", example = "John Doe")
    String name

    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "Email must be less than 50 characters")
    @Email
    @Pattern(regexp = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$', message = "enter valid mail id only")
    @Schema(description = "Gmail address", example = "johndoe@gmail.com")
    String email

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    @Schema(description = "Account password", example = "Str0ngP@ss")
    @Pattern(regexp= '^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$' , message="enter valid password only")
    String password

    @NotNull(message = "Role is required")
    @Schema(description = "User role", example = "OWNER")
    Role role
}