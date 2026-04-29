package in.respondlyai.auth.service

import groovy.util.logging.Slf4j
import in.respondlyai.auth.dto.OrganizationCreatedEvent
import in.respondlyai.auth.entity.User
import in.respondlyai.auth.repository.UserRepository
import in.respondlyai.auth.repository.RoleRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
class KafkaConsumerService {

    private final UserRepository userRepository
    private final RoleRepository roleRepository
    private final EmailService emailService

    KafkaConsumerService(UserRepository userRepository, RoleRepository roleRepository, EmailService emailService) {
        this.userRepository = userRepository
        this.roleRepository = roleRepository
        this.emailService = emailService
    }

    @KafkaListener(topics = "org-events", groupId = "auth-group")
    @Transactional
    void handleOrganizationCreated(OrganizationCreatedEvent event) {
        log.info("Received OrganizationCreatedEvent for Org ID: {}", event.organizationId)

        try {
            // 1. Update Owner's Organization ID
            userRepository.findById(event.ownerUserId).ifPresentOrElse(
                { owner ->
                    owner.organizationId = event.organizationId.toString()
                    userRepository.save(owner)
                    log.info("Updated organization ID for owner: {}", event.ownerUserId)
                },
                { log.warn("Owner user not found: {}", event.ownerUserId) }
            )

            // 2. Process Invited Users
            event.invitedUsers?.each { invitedUser ->
                processInvitedUser(event.organizationId.toString(), invitedUser)
            }

        } catch (Exception e) {
            log.error("Error processing OrganizationCreatedEvent: {}", e.message, e)
        }
    }

    private void processInvitedUser(String orgId, OrganizationCreatedEvent.InvitedUser invited) {
        if (userRepository.existsByEmail(invited.email)) {
            log.info("Invited user already exists: {}. Skipping creation.", invited.email)
            return
        }

        log.info("Creating placeholder account for invited user: {}", invited.email)
        
        // Find role
        def role = roleRepository.findByName(invited.role).orElseGet {
            log.warn("Role {} not found, defaulting to MEMBER", invited.role)
            roleRepository.findByName("MEMBER").orElse(null)
        }

        if (!role) {
            log.error("Could not find any suitable role for invited user: {}", invited.email)
            return
        }

        User newUser = new User()
        newUser.email = invited.email
        newUser.name = invited.email.split("@")[0]
        newUser.password = "INVITED_USER_PLACEHOLDER" 
        newUser.organizationId = orgId
        newUser.role = role
        newUser.isVerified = false
        
        userRepository.save(newUser)
        log.info("Placeholder user created: {}", invited.email)

        // Send invitation email
        emailService.sendOrganizationInviteEmail(invited.email, invited.role, orgId)
    }
}
