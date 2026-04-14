package in.respondlyai.auth.repository

import in.respondlyai.auth.entity.Token
import in.respondlyai.auth.entity.TokenStatus
import in.respondlyai.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import java.util.Optional
import java.util.List

interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String token)
    List<Token> findAllByUserAndStatus(User user, TokenStatus status)
}
