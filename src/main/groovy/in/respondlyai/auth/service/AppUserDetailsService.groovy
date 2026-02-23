package in.respondlyai.auth.service

import in.respondlyai.auth.entity.User
import in.respondlyai.auth.repository.UserRepository
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
@Service
@Primary
class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository

    AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow({ new UsernameNotFoundException("User not found with email: ${email}") })

        return new org.springframework.security.core.userdetails.User(
                user.email,
                user.password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_${user.role.name()}"))
        )
    }
}
