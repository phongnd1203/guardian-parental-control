package app.guardian.android.feature.family.domain.model

import java.time.Instant

/**
 * Pure domain representation of a Device's permission status.
 */
data class DevicePermissionStatus(
    val deviceId: String,
    val usageAccess: Boolean = false,
    val accessibilityService: Boolean = false,
    val notificationPermission: Boolean = false,
    val vpnActive: Boolean = false,
    val locationPermission: Boolean = false,
    val deviceAdmin: Boolean = false,
    val updatedAt: Instant? = null
) {
    /**
     * All critical permissions are granted.
     */
    val isFullyConfigured: Boolean
        get() = usageAccess && accessibilityService && notificationPermission && deviceAdmin
}
