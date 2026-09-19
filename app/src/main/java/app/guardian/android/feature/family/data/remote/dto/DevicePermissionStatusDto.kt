package app.guardian.android.feature.family.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DevicePermissionStatusDto(
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("usage_access")
    val usageAccess: Boolean = false,
    @SerialName("accessibility_service")
    val accessibilityService: Boolean = false,
    @SerialName("notification_permission")
    val notificationPermission: Boolean = false,
    @SerialName("vpn_active")
    val vpnActive: Boolean = false,
    @SerialName("location_permission")
    val locationPermission: Boolean = false,
    @SerialName("device_admin")
    val deviceAdmin: Boolean = false,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
