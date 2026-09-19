package app.guardian.android.feature.family.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChildDto(
    @SerialName("id")
    val id: String,
    @SerialName("family_id")
    val familyId: String,
    @SerialName("name")
    val name: String,
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: String? = null,
    @SerialName("avatar_path")
    val avatarPath: String? = null,
    @SerialName("status")
    val status: String = "ACTIVE",
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("archived_at")
    val archivedAt: String? = null,
    @SerialName("deleted_at")
    val deletedAt: String? = null
)
