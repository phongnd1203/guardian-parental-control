package app.guardian.android.feature.family.domain.model

import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.InvitationStatus
import java.time.Instant

/**
 * Pure domain representation of a Family Invitation.
 */
data class FamilyInvitation(
    val id: String,
    val familyId: String,
    val email: String,
    val role: FamilyRole,
    val status: InvitationStatus,
    val expiresAt: Instant,
    val invitedBy: String,
    val createdAt: Instant,
    val acceptedBy: String? = null,
    val acceptedAt: Instant? = null
) {
    val isExpired: Boolean
        get() = Instant.now().isAfter(expiresAt)

    val isPending: Boolean
        get() = status == InvitationStatus.PENDING && !isExpired
}
