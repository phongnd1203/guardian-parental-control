package app.guardian.android.feature.family.domain.model

import app.guardian.android.core.common.DateTimeUtils
import app.guardian.android.core.model.DeviceOnlineStatus
import app.guardian.android.core.model.ProtectionStatus
import java.time.Instant

/**
 * Pure domain representation of a Child's Device.
 */
data class Device(
    val id: String,
    val familyId: String,
    val childId: String,
    val authUserId: String? = null,
    val name: String,
    val manufacturer: String? = null,
    val model: String? = null,
    val androidVersion: String? = null,
    val apiLevel: Int? = null,
    val appVersion: String? = null,
    val appBuild: Int? = null,
    val batteryLevel: Int? = null,
    val charging: Boolean = false,
    val protectionStatus: ProtectionStatus = ProtectionStatus.ACTIVE,
    val lastSeenAt: Instant? = null,
    val pairedAt: Instant? = null,
    val permissionStatus: DevicePermissionStatus? = null
) {
    /**
     * Connection status: ONLINE (<= 2 min), RECENTLY_ONLINE (2-15 min), OFFLINE (> 15 min).
     */
    val onlineStatus: DeviceOnlineStatus
        get() = DateTimeUtils.calculateOnlineStatus(lastSeenAt)

    /**
     * Human-friendly last seen string.
     */
    val lastSeenText: String
        get() = DateTimeUtils.formatLastSeenText(lastSeenAt)

    /**
     * Device model and manufacturer description: e.g. "Samsung SM-S938B"
     */
    val hardwareDescription: String
        get() = listOfNotNull(manufacturer, model).filter { it.isNotBlank() }.joinToString(" ")
}
