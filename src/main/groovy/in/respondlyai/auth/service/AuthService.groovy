package in.respondlyai.auth.service

import in.respondlyai.auth.dto.AuthResponse
import in.respondlyai.auth.dto.LoginRequest
import in.respondlyai.auth.dto.SignupRequest
import in.respondlyai.auth.entity.Role
import in.respondlyai.auth.entity.User
import in.respondlyai.auth.exception.ApiException
import in.respondlyai.auth.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService)

    private final UserRepository userRepository
    private final PasswordEncoder passwordEncoder

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository
        this.passwordEncoder = passwordEncoder
    }

    @Transactional(readOnly = true)
    AuthResponse login(LoginRequest request) {
        // Manual simplified validation
        if (!request.email || !request.password) {
            throw ApiException.badRequest("Email and password are required")
        }

        if (request.password.length() < 6) {
            throw ApiException.badRequest("Password must be between 6 and 40 characters")
        }

        // Find user by email
        User user = userRepository.findByEmail(request.email)
                .orElseThrow({ ApiException.authError("Invalid email..") })

        // Verify password
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ApiException.authError("Invalid password")
        }

        log.info("User logged in successfully: userId={}, email={}", user.userId, user.email)

        return new AuthResponse(
                null, // token will be generated later
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        )
    }

    @Transactional
    AuthResponse signup(SignupRequest request) {

        // Only OWNER role can signup
        if (request.role != Role.OWNER) {
            throw ApiException.forbidden("Only users with role OWNER can signup")
        }

        // Check for existing email
        if (userRepository.existsByEmail(request.email)) {
            throw ApiException.conflict("Email already in use")
        }

        try {
            User user = new User()
            user.setUserId(UUID.randomUUID().toString())
            user.setName(request.name)
            user.setEmail(request.email)
            user.setPassword(passwordEncoder.encode(request.password))
            user.setRole(Role.OWNER)

            User savedUser = userRepository.save(user)

            log.info("User created successfully: userId={}", savedUser.userId)

            return new AuthResponse(
                    null,
                    savedUser.getUserId(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            )
        } catch (Exception ex) {
            if (ex instanceof ApiException) throw ex
            log.error("Signup error: {}", ex.message)
            throw ApiException.internalError("Failed to create user account")
        }
    }
}