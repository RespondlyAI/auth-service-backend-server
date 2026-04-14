package in.respondlyai.auth.repository

import in.respondlyai.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import java.util.Optional

interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email)
    boolean existsByEmail(String email)
}