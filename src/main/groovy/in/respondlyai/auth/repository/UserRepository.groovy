package in.respondlyai.auth.repository

import in.respondlyai.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email)

    boolean existsByEmail(String email)

    Optional<User> findByUserId(String userId)
}