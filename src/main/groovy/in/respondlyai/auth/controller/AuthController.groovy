package in.respondlyai.auth.controller

import in.respondlyai.auth.dto.SignupRequest
import in.respondlyai.auth.dto.AuthResponse
import in.respondlyai.auth.exception.ApiErrorResponse
import in.respondlyai.auth.service.AuthService

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for RespondlyAI")
class AuthController {

    private final AuthService authService

    AuthController(AuthService authService) {
        this.authService = authService
    }

    @PostMapping("/signup")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. Only users with role OWNER can signup. Email must be a valid @gmail.com address."
    )

// api docs
    @ApiResponses([
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = '{"success":false,"message":"Name is required, Password must be between 6 and 40 characters","type":"VALIDATION_ERROR","timestamp":"2026-02-21T14:30:00.000Z","metadata":{"fields":{"name":"Name is required","password":"Password must be between 6 and 40 characters"}},"path":"/api/auth/signup","method":"POST"}'
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden — only OWNER role allowed",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = '{"success":false,"message":"Only users with role OWNER can signup","type":"FORBIDDEN","timestamp":"2026-02-21T14:30:00.000Z","path":"/api/auth/signup","method":"POST"}'
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict — email already in use",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse),
                            examples = @ExampleObject(
                                    name = "Conflict",
                                    value = '{"success":false,"message":"Email already in use","type":"CONFLICT","timestamp":"2026-02-21T14:30:00.000Z","path":"/api/auth/signup","method":"POST"}'
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse),
                            examples = @ExampleObject(
                                    name = "Internal Error",
                                    value = '{"success":false,"message":"Failed to create user account","type":"INTERNAL_SERVER_ERROR","timestamp":"2026-02-21T14:30:00.000Z","path":"/api/auth/signup","method":"POST"}'
                            )
                    )
            )
    ])


    ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}