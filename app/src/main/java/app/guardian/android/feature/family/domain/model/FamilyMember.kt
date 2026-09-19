package app.guardian.android.feature.family.domain.model

import app.guardian.android.core.model.FamilyRole
import java.time.Instant

/**
 * Pure domain representation of a Family Member.
 */
data class FamilyMember(
    val id: String,
    val familyId: String,
    val userId: String,
    val role: FamilyRole,
    val email: String? = null,
    val joinedAt: Instant? = null,
    val createdAt: Instant? = null
) {
    val isOwner: Boolean
        get() = role == FamilyRole.OWNER

    val isParent: Boolean
        get() = role == FamilyRole.PARENT

    val isViewer: Boolean
        get() = role == FamilyRole.VIEWER
}
