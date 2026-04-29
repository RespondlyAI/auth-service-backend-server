package in.respondlyai.auth.dto

import groovy.transform.Canonical
import java.util.UUID

@Canonical
class OrganizationCreatedEvent {
    UUID organizationId
    UUID ownerUserId
    List<InvitedUser> invitedUsers

    OrganizationCreatedEvent() {}

    @Canonical
    static class InvitedUser {
        String email
        String role

        InvitedUser() {}
    }
}
