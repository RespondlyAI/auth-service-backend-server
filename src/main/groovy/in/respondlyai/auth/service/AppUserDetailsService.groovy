package in.respondlyai.auth.service

import in.respondlyai.auth.entity.User
import in.respondlyai.auth.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AppUserDetailsService)

    private final UserRepository userRepository

    AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email)
        User user = userRepository.findByEmail(email)
                .orElseThrow({
                    log.warn("User not found for email: {}", email)
                    new UsernameNotFoundException("User not found with email: " + email)
                })
        log.debug("User loaded: userId={}, role={}", user.id, user.role.name)
        return toUserDetails(user)
    }

    static UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.email)
                .password(user.password)
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.role.name))
                .build()
    }
}
