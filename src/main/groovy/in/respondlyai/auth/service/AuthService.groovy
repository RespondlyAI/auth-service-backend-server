package in.respondlyai.auth.service

import in.respondlyai.auth.dto.*
import in.respondlyai.auth.entity.*
import in.respondlyai.auth.exception.ApiException
import in.respondlyai.auth.repository.*
import in.respondlyai.auth.security.jwt.JwtService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService)

    private final UserRepository userRepository
    private final RoleRepository roleRepository
    private final TokenRepository tokenRepository
    private final TokenTypeRepository tokenTypeRepository
    private final PasswordEncoder passwordEncoder
    private final JwtService jwtService
    private final ThunderMailService thunderMailService
    private final OtpService otpService

    @org.springframework.beans.factory.annotation.Value('${application.security.jwt.expiration}')
    private long jwtExpiration

    @org.springframework.beans.factory.annotation.Value('${application.security.jwt.refresh-token.expiration}')
    private long refreshExpiration

    AuthService(UserRepository userRepository, RoleRepository roleRepository,
                TokenRepository tokenRepository, TokenTypeRepository tokenTypeRepository,
                PasswordEncoder passwordEncoder, JwtService jwtService,
                ThunderMailService thunderMailService, OtpService otpService) {
        this.userRepository = userRepository
        this.roleRepository = roleRepository
        this.tokenRepository = tokenRepository
        this.tokenTypeRepository = tokenTypeRepository
        this.passwordEncoder = passwordEncoder
        this.jwtService = jwtService
        this.thunderMailService = thunderMailService
        this.otpService = otpService
    }

    @Transactional
    AuthResponse login(LoginRequest request) {
        // Manual simplified validation
        if (!request.email || !request.password) {
            throw ApiException.badRequest("Email and password are required")
        }

        if (request.password.length() < 6) {
            throw ApiException.badRequest("Password must be between 6 and 40 characters")
        }

        log.debug("Login attempt: email={}", request.email)

        // Find user by email
        User user = userRepository.findByEmail(request.email)
                .orElseThrow({ ApiException.authError("Invalid email") })

        log.debug("User found: userId={}, verified={}", user.id, user.isVerified)

        if (!passwordEncoder.matches(request.password, user.password)) {
            log.warn("Login failed - wrong password: email={}", request.email)
            throw ApiException.authError("Invalid password")
        }

        if (!user.isVerified) {
            log.warn("Login failed - unverified user: userId={}", user.id)
            throw ApiException.forbidden("Please verify your email using the OTP sent to you.")
        }

        // Update last login
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        log.info("User logged in successfully: userId={}, email={}", user.id, user.email)

        return generateAuthResponse(user)
    }

    @Transactional
    AuthResponse signup(SignupRequest request) {
        // Check for existing email
        if (userRepository.existsByEmail(request.email)) {
            throw ApiException.conflict("Email already in use")
        }

        try {
            Role ownerRole = roleRepository.findByName("OWNER")
                    .orElseThrow({ ApiException.internalError("Default roles not initialized") })

            User user = new User()
            user.setName(request.name)
            user.setEmail(request.email)
            user.setPassword(passwordEncoder.encode(request.password))
            user.setRole(ownerRole)

            User savedUser = userRepository.save(user)

            String otp = otpService.generateAndStoreOtp(savedUser.id.toString())
            log.info("User created successfully (unverified): userId={}", savedUser.id)

            thunderMailService.sendOtpEmail(savedUser.email, otp)

            return new AuthResponse(
                    null,
                    null,
                    savedUser.id,
                    savedUser.email,
                    savedUser.role.name
            )
        } catch (Exception ex) {
            if (ex instanceof ApiException) throw ex
            log.error("Signup error: {}", ex.message)
            throw ApiException.internalError("Failed to create user account")
        }
    }

    @Transactional
    void verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findById(request.id)
                .orElseThrow({ ApiException.authError("User not found") })

        if (user.isVerified) {
            throw ApiException.badRequest("User is already verified")
        }

        if (!otpService.validateOtp(request.id.toString(), request.otp)) {
            throw ApiException.authError("Invalid or expired OTP")
        }

        user.isVerified = true
        userRepository.save(user)

        log.info("User verified successfully: userId={}", user.id)
    }

    // Helper method to generate access and refresh tokens and save them to DB
    private AuthResponse generateAuthResponse(User user) {
        UserDetails userDetails = AppUserDetailsService.toUserDetails(user)
        String accessToken = jwtService.generateAccessToken(user, userDetails)
        String refreshToken = jwtService.generateRefreshToken(user, userDetails)

        TokenType accessType = tokenTypeRepository.findByName("ACCESS")
                .orElseThrow({ ApiException.internalError("Token types not initialized") })
        TokenType refreshType = tokenTypeRepository.findByName("REFRESH")
                .orElseThrow({ ApiException.internalError("Token types not initialized") })

        Token accessTokenEntity = new Token()
        accessTokenEntity.user = user
        accessTokenEntity.token = accessToken
        accessTokenEntity.status = TokenStatus.active
        accessTokenEntity.tokenType = accessType
        accessTokenEntity.expiresAt = LocalDateTime.now().plusSeconds(jwtExpiration / 1000)
        tokenRepository.save(accessTokenEntity)

        Token refreshTokenEntity = new Token()
        refreshTokenEntity.user = user
        refreshTokenEntity.token = refreshToken
        refreshTokenEntity.status = TokenStatus.active
        refreshTokenEntity.tokenType = refreshType
        refreshTokenEntity.expiresAt = LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        tokenRepository.save(refreshTokenEntity)

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.id,
                user.email,
                user.role.name
        )
    }

    @Transactional
    AuthResponse refreshToken(String refreshToken) {
        // Step 1: Basic JWT validation
        String email = jwtService.extractEmail(refreshToken)
        User user = userRepository.findByEmail(email)
                .orElseThrow({ ApiException.authError("Invalid session") })

        // Step 2: Fetch token from DB
        Token tokenEntity = tokenRepository.findByToken(refreshToken)
                .orElseThrow({ ApiException.authError("Token not found or fake") })

        // Step 3: Check status and expiry
        if (tokenEntity.status != TokenStatus.active) {
            log.warn("Attempt to use a non-active token: userId={}, status={}", user.id, tokenEntity.status)
            tokenRepository.delete(tokenEntity)
            throw ApiException.authError("Token has been revoked or already used")
        }

        if (tokenEntity.expiresAt.isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity)
            throw ApiException.authError("Token has expired")
        }

        log.info("Issuing new access token for user: userId={}", user.id)

        UserDetails userDetails = AppUserDetailsService.toUserDetails(user)
        String newAccessToken = jwtService.generateAccessToken(user, userDetails)

        TokenType accessType = tokenTypeRepository.findByName("ACCESS")
                .orElseThrow({ ApiException.internalError("Token types not initialized") })
        
        Token accessTokenEntity = new Token()
        accessTokenEntity.user = user
        accessTokenEntity.token = newAccessToken
        accessTokenEntity.status = TokenStatus.active
        accessTokenEntity.tokenType = accessType
        accessTokenEntity.expiresAt = LocalDateTime.now().plusSeconds(jwtExpiration / 1000)
        tokenRepository.save(accessTokenEntity)

        return new AuthResponse(
                newAccessToken,
                refreshToken,
                user.id,
                user.email,
                user.role.name
        )
    }

    @Transactional
    void logout(String refreshToken) {
        Token tokenEntity = tokenRepository.findByToken(refreshToken)
                .orElseThrow({ ApiException.authError("Token not found") })

        tokenRepository.delete(tokenEntity)
        log.info("User logged out successfully: userId={}", tokenEntity.user.id)
    }
}