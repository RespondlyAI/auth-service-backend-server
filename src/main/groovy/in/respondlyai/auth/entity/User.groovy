package in.respondlyai.auth.entity

import jakarta.persistence.*
import java.util.UUID
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User {

    @Id
    @org.hibernate.annotations.UuidGenerator
    @Column(name = "uuid", updatable = false, nullable = false)
    UUID uuid

    @Column(name = "user_id", nullable = false, unique = true)
    String userId

    @Column(name = "name", nullable = false)
    String name

    @Column(name = "email", nullable = false, unique = true)
    String email

    @Column(name = "password", nullable = false)
    String password

    @Column(name = "is_verified", nullable = false)
    Boolean isVerified = false

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    Role role = Role.OWNER

    @Column(name = "organization_id")
    String organizationId

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now()
    }

}