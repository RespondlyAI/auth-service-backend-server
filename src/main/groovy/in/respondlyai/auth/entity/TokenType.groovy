package in.respondlyai.auth.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "token_types")
class TokenType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id

    @Column(unique = true, nullable = false)
    String name

    TokenType() {}

    TokenType(String name) {
        this.name = name
    }
}
