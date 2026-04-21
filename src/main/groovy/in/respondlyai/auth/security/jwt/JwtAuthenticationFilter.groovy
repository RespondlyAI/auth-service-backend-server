package in.respondlyai.auth.security.jwt

import groovy.util.logging.Slf4j
import in.respondlyai.auth.service.AppUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that intercepts incoming requests to check for a valid JWT token in the Authorization header.
 * If a valid token is found, it populates the SecurityContext with the user's authentication details.
 */
@Slf4j
@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService
    private final AppUserDetailsService userDetailsService

    JwtAuthenticationFilter(JwtService jwtService, AppUserDetailsService userDetailsService) {
        this.jwtService = jwtService
        this.userDetailsService = userDetailsService
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization")
        final String jwt
        final String userEmail

        // Skip filter if no Bearer token is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            jwt = authHeader.substring(7)
            userEmail = jwtService.extractEmail(jwt)

            // Authenticate user if token is present and security context is empty
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail)

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    )
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request))
                    SecurityContextHolder.getContext().setAuthentication(authToken)
                    log.debug("Successfully authenticated user: {}", userEmail)
                }
            }
        } catch (Exception ex) {
            log.warn("Invalid JWT token: {}", ex.message)
        }

        filterChain.doFilter(request, response)
    }
}
