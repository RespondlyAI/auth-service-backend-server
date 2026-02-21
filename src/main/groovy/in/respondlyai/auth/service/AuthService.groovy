package in.respondlyai.auth.service

import in.respondlyai.auth.dto.AuthResponse
import in.respondlyai.auth.dto.SignupRequest
import in.respondlyai.auth.entity.Role
import in.respondlyai.auth.entity.User
import in.respondlyai.auth.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService {

    private final UserRepository userRepository

    AuthService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Transactional
    AuthResponse signup(SignupRequest request) {
        if (request.role != Role.OWNER) {
            throw new RuntimeException("Only users with role OWNER can signup")
        }

        if (userRepository.existsByEmail(request.email)) {
            throw new RuntimeException("User already exists")
        }

        User user = new User()
        user.setUserId(UUID.randomUUID().toString())
        user.setName(request.name)
        user.setEmail(request.email)
        user.setPassword(request.password)
        user.setRole(Role.OWNER)

        User savedUser = userRepository.save(user)

        return new AuthResponse(
                null,
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getRole()
        )
    }
}
                                                                                                                                                                               