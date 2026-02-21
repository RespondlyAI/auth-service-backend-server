package in.respondlyai.auth.dto

import in.respondlyai.auth.entity.Role

class AuthResponse {
    String token
    String type = "Bearer"
    String id
    String email
    Role role

    AuthResponse(String token, String id, String email, Role role) {
        this.token = token
        this.id = id
        this.email = email
        this.role = role
    }
}
