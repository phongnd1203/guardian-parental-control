package app.guardian.android.feature.family.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    @SerialName("id")
    val id: String,
    @SerialName("family_id")
    val familyId: String,
    @SerialName("child_id")
    val childId: String,
    @SerialName("auth_user_id")
    val authUserId: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("manufacturer")
    val manufacturer: String? = null,
    @SerialName("model")
    val model: String? = null,
    @SerialName("android_version")
    val androidVersion: String? = null,
    @SerialName("api_level")
    val apiLevel: Int? = null,
    @SerialName("app_version")
    val appVersion: String? = null,
    @SerialName("app_build")
    val appBuild: Int? = null,
    @SerialName("battery_level")
    val batteryLevel: Int? = null,
    @SerialName("charging")
    val charging: Boolean = false,
    @SerialName("protection_status")
    val protectionStatus: String = "ACTIVE",
    @SerialName("last_seen_at")
    val lastSeenAt: String? = null,
    @SerialName("paired_at")
    val pairedAt: String? = null,
    @SerialName("unpaired_at")
    val unpairedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
