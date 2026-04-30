package in.respondlyai.auth.entity

import jakarta.persistence.*
import java.util.UUID
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id

    @Column(name = "name", nullable = false)
    String name

    @Column(name = "email", nullable = false, unique = true)
    String email

    @Column(name = "password", nullable = false)
    String password

    @Column(name = "is_verified", nullable = false)
    Boolean isVerified = false

    @Column(name = "role_id", nullable = false)
    UUID roleId

    @Column(name = "organization_id")
    String organizationId

    @Column(name = "last_login_at")
    LocalDateTime lastLoginAt

    @Column(name = "password_updated_at")
    LocalDateTime passwordUpdatedAt

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now()
        if (updatedAt == null) updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}