package in.respondlyai.auth.service

import groovy.util.logging.Slf4j
import in.respondlyai.auth.entity.User
import in.respondlyai.auth.repository.UserRepository
import in.respondlyai.auth.repository.RoleRepository
import org.springframework.context.annotation.Primary
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * UserDetailsService implementation that loads users from the database.
 * Marked as @Primary to override Spring Security's default InMemoryUserDetailsManager.
 */
@Slf4j
@Service
@Primary
class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository
    private final RoleRepository roleRepository

    AppUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository
        this.roleRepository = roleRepository
    }

    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email)
        User user = userRepository.findByEmail(email)
                .orElseThrow({ 
                    log.warn("User not found with email: {}", email)
                    new UsernameNotFoundException("User not found with email: ${email}") 
                })
                
        String roleName = roleRepository.findById(user.roleId)
                .map({ it.name })
                .orElse("UNKNOWN")

        return toUserDetails(user, roleName)
    }

    /**
     * Converts a User entity to a Spring Security UserDetails object.
     * Shared by both this service and AuthService to avoid duplicating the mapping logic.
     */
    static UserDetails toUserDetails(User user, String roleName) {
        return new org.springframework.security.core.userdetails.User(
                user.email,
                user.password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_${roleName}"))
        )
    }
}
