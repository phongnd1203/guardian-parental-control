package app.guardian.android.feature.family.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvitationDto(
    @SerialName("id")
    val id: String,
    @SerialName("family_id")
    val familyId: String,
    @SerialName("email")
    val email: String,
    @SerialName("role")
    val role: String,
    @SerialName("token_hash")
    val tokenHash: String,
    @SerialName("status")
    val status: String,
    @SerialName("expires_at")
    val expiresAt: String,
    @SerialName("invited_by")
    val invitedBy: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("accepted_by")
    val acceptedBy: String? = null,
    @SerialName("accepted_at")
    val acceptedAt: String? = null,
    @SerialName("cancelled_at")
    val cancelledAt: String? = null
)
