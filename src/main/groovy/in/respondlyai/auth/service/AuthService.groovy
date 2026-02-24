package in.respondlyai.auth.service

import in.respondlyai.auth.dto.AuthResponse
import in.respondlyai.auth.dto.LoginRequest
import in.respondlyai.auth.dto.SignupRequest
import in.respondlyai.auth.entity.Role
import in.respondlyai.auth.entity.User
import in.respondlyai.auth.exception.ApiException
import in.respondlyai.auth.repository.UserRepository
import in.respondlyai.auth.security.jwt.JwtService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService)

    private final UserRepository userRepository
    private final PasswordEncoder passwordEncoder
    private final JwtService jwtService

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                JwtService jwtService) {
        this.userRepository = userRepository
        this.passwordEncoder = passwordEncoder
        this.jwtService = jwtService
    }

    /**
     * Builds a Spring Security UserDetails from a User entity.
     * Avoids a redundant database round-trip compared to calling loadUserByUsername.
     */
    private static UserDetails toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.email,
                user.password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_${user.role.name()}"))
        )
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

        // Generate JWT token
        UserDetails userDetails = toUserDetails(user)
        String token = jwtService.generateToken(userDetails, user.userId, user.role.name())

        log.info("User logged in successfully: userId={}, email={}", user.userId, user.email)

        return new AuthResponse(
                token,
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

            // Generate JWT token for the new user
            UserDetails userDetails = toUserDetails(savedUser)
            String token = jwtService.generateToken(userDetails, savedUser.userId, savedUser.role.name())

            log.info("User created successfully: userId={}", savedUser.userId)

            return new AuthResponse(
                    token,
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