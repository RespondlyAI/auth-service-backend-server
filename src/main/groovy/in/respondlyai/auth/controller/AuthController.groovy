package in.respondlyai.auth.controller


import in.respondlyai.auth.dto.SignupRequest
import in.respondlyai.auth.dto.AuthResponse
import in.respondlyai.auth.service.AuthService

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController {
    
    private final AuthService authService

    AuthController(AuthService authService){
        this.authService = authService
    }

    @PostMapping("/signup")
    ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request){

        AuthResponse response = authService.signup(request)
        return ResponseEntity.ok(response)
    }
}