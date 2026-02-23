package in.respondlyai.auth.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.lang.NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Intercepts every incoming request to check if the user has a valid JWT.
 */
@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService
    private final UserDetailsService userDetailsService

    JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService
        this.userDetailsService = userDetailsService
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization")
        final String jwt
        final String userEmail

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        jwt = authHeader.substring(7)
        
        userEmail = jwtService.extractEmail(jwt)

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Load user from database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail)

            // If the token is valid
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
        filterChain.doFilter(request, response)
    }
}
