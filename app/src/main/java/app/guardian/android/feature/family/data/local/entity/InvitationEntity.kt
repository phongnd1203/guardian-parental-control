package app.guardian.android.feature.family.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.InvitationStatus
import java.time.Instant

@Entity(
    tableName = "family_invitations",
    indices = [
        Index(value = ["family_id"]),
        Index(value = ["family_id", "email", "status"])
    ]
)
data class InvitationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "family_id")
    val familyId: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "role")
    val role: FamilyRole = FamilyRole.VIEWER,

    @ColumnInfo(name = "token_hash")
    val tokenHash: String,

    @ColumnInfo(name = "status")
    val status: InvitationStatus = InvitationStatus.PENDING,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Instant,

    @ColumnInfo(name = "invited_by")
    val invitedBy: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "accepted_by")
    val acceptedBy: String? = null,

    @ColumnInfo(name = "accepted_at")
    val acceptedAt: Instant? = null,

    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Instant? = null
)
