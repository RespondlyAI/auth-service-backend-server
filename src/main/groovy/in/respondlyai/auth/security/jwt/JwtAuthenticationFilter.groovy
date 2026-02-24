package in.respondlyai.auth.security.jwt

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.lang.NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Intercepts every incoming request to check if the user has a valid JWT.
 */
@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter)

    private final JwtService jwtService

    JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            final String jwt = authHeader.substring(7)

            // Throws JwtException for malformed / expired / unsupported tokens.
            // Signature verification also happens here, so the claims are trustworthy.
            final String userEmail = jwtService.extractEmail(jwt)
            final String role = jwtService.extractRole(jwt)

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Build UserDetails from JWT claims — no DB call needed for stateless auth.
                // Token integrity is already guaranteed by the signature check above.
                UserDetails userDetails = new User(
                        userEmail, "",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_${role}"))
                )

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    )
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request))
                    SecurityContextHolder.getContext().setAuthentication(authToken)
                }
            }
        } catch (JwtException ex) {
            log.warn("Invalid JWT token [{}]: {}", request.getRequestURI(), ex.getMessage())
        } catch (Exception ex) {
            log.error("Unexpected error during JWT authentication [{}]", request.getRequestURI(), ex)
        }

        filterChain.doFilter(request, response)
    }
}
