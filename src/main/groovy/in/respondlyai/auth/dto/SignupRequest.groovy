package in.respondlyai.auth.dto

import in.respondlyai.auth.model.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.NotNull


class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
    private String name

    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "Email must be less than 50 characters")
    @Email
    private String email

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password

    @NotNull(message = "Role is required")
    private Set<String> role
}