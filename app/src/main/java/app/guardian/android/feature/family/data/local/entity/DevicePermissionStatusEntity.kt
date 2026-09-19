package app.guardian.android.feature.family.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "device_permission_status"
)
data class DevicePermissionStatusEntity(
    @PrimaryKey
    @ColumnInfo(name = "device_id")
    val deviceId: String,

    @ColumnInfo(name = "usage_access")
    val usageAccess: Boolean = false,

    @ColumnInfo(name = "accessibility_service")
    val accessibilityService: Boolean = false,

    @ColumnInfo(name = "notification_permission")
    val notificationPermission: Boolean = false,

    @ColumnInfo(name = "vpn_active")
    val vpnActive: Boolean = false,

    @ColumnInfo(name = "location_permission")
    val locationPermission: Boolean = false,

    @ColumnInfo(name = "device_admin")
    val deviceAdmin: Boolean = false,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null
)
