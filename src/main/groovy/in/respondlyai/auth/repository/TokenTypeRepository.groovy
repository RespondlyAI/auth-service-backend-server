package in.respondlyai.auth.repository

import in.respondlyai.auth.entity.TokenType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import java.util.Optional

interface TokenTypeRepository extends JpaRepository<TokenType, UUID> {
    Optional<TokenType> findByName(String name)
}
