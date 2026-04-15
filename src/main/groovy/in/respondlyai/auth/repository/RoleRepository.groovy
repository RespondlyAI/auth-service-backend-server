package in.respondlyai.auth.repository

import in.respondlyai.auth.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID
import java.util.Optional

interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name)
}
