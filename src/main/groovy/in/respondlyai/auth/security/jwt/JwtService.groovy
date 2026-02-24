package in.respondlyai.auth.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

import javax.crypto.SecretKey
import java.nio.charset.StandardCharsets
import java.util.function.Function

/**
 jwt service 
 */
@Service
class JwtService {

    @Value('${application.security.jwt.secret-key}')
    private String secretKey

    @Value('${application.security.jwt.expiration}')
    private long jwtExpiration

    private SecretKey signingKey

    @PostConstruct
    void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8)
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "JWT secret key is too weak: must be at least 32 bytes (256 bits), " +
                "but got ${keyBytes.length} bytes. Update 'application.security.jwt.secret-key'.")
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * Extracts email
     */
    String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject)
    }

    /**
     * Generic method to extract any claim from the token.
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    /**
     * Generates a new JWT token for a specific user.
     */
    String generateToken(UserDetails userDetails, String userId, String role) {
        Map<String, Object> claims = new HashMap<>()
        claims.put("role", role)
        claims.put("userId", userId)
        return generateToken(claims, userDetails)
    }

    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // userDetails.getUsername() returns email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact()
    }

    /**
     * Checks if the token belongs to the right user and hasn't expired.
     */
    boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token)
        return (email == userDetails.getUsername()) && !isTokenExpired(token)
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date())
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration)
    }

    /**
     * Parses the token using the secretKey.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
    }

    /**
     * Returns the cached signing key, pre-validated at startup.
     */
    private SecretKey getSignInKey() {
        return signingKey
    }
}
