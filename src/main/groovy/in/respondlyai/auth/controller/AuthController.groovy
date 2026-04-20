package in.respondlyai.auth.controller

import in.respondlyai.auth.dto.*
import in.respondlyai.auth.exception.ApiErrorResponse
import in.respondlyai.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
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
    @Operation(summary = "Register a new user", description = "Creates a new user account. Only users with role OWNER can signup.")
    @ApiResponses([
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = AuthResponse))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiErrorResponse))),
            @ApiResponse(responseCode = "403", description = "Forbidden — only OWNER role allowed", content = @Content(schema = @Schema(implementation = ApiErrorResponse))),
            @ApiResponse(responseCode = "409", description = "Conflict — email already in use", content = @Content(schema = @Schema(implementation = ApiErrorResponse)))
    ])
    ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verifies the 6-digit OTP.")
    @ApiResponses([
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired OTP", content = @Content(schema = @Schema(implementation = ApiErrorResponse)))
    ])
    ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request)
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully. Please login to continue."))
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Resends the 6-digit OTP to the user's email if they are not verified.")
    @ApiResponses([
            @ApiResponse(responseCode = "200", description = "OTP resent successfully"),
            @ApiResponse(responseCode = "400", description = "User is already verified", content = @Content(schema = @Schema(implementation = ApiErrorResponse))),
            @ApiResponse(responseCode = "401", description = "User not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse)))
    ])
    ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request)
        return ResponseEntity.ok(Map.of("message", "OTP resent successfully. Please check your email."))
    }

    @PostMapping("/login")
    @Operation(summary = "Login existing user", description = "Authenticates user and returns access and refresh tokens.")
    @ApiResponses([
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — invalid credentials", content = @Content(schema = @Schema(implementation = ApiErrorResponse)))
    ])
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request)
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.token)
                .body(response)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Uses a valid refresh token to generate a new pair of tokens.")
    @ApiResponses([
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponse))),
            @ApiResponse(responseCode = "401", description = "Unauthorized — invalid or expired refresh token", content = @Content(schema = @Schema(implementation = ApiErrorResponse)))
    ])
    ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.token)
                .body(response)
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes the provided refresh token.")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken)
        return ResponseEntity.noContent().build()
    }
}