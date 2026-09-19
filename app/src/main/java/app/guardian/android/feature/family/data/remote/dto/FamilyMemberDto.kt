package app.guardian.android.feature.family.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamilyMemberDto(
    @SerialName("id")
    val id: String,
    @SerialName("family_id")
    val familyId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("role")
    val role: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("joined_at")
    val joinedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
