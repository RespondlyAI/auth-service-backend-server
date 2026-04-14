package in.respondlyai.auth.entity

import jakarta.persistence.*
import java.util.UUID
import java.time.LocalDateTime

enum TokenStatus {
    active, used, revoked, expired
}

@Entity
@Table(name = "tokens")
class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user

    @Column(unique = true, nullable = false, length = 1024)
    String token

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TokenStatus status = TokenStatus.active

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt = LocalDateTime.now()

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt

    @Column(name = "used_at")
    LocalDateTime usedAt

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "token_type_id", nullable = false)
    TokenType tokenType
}
