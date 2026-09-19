package app.guardian.android.feature.family.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.guardian.android.core.model.ProtectionStatus
import java.time.Instant

@Entity(
    tableName = "devices",
    indices = [
        Index(value = ["child_id"]),
        Index(value = ["family_id"]),
        Index(value = ["auth_user_id"]),
        Index(value = ["last_seen_at"])
    ]
)
data class DeviceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "family_id")
    val familyId: String,

    @ColumnInfo(name = "child_id")
    val childId: String,

    @ColumnInfo(name = "auth_user_id")
    val authUserId: String? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "manufacturer")
    val manufacturer: String? = null,

    @ColumnInfo(name = "model")
    val model: String? = null,

    @ColumnInfo(name = "android_version")
    val androidVersion: String? = null,

    @ColumnInfo(name = "api_level")
    val apiLevel: Int? = null,

    @ColumnInfo(name = "app_version")
    val appVersion: String? = null,

    @ColumnInfo(name = "app_build")
    val appBuild: Int? = null,

    @ColumnInfo(name = "battery_level")
    val batteryLevel: Int? = null,

    @ColumnInfo(name = "charging")
    val charging: Boolean = false,

    @ColumnInfo(name = "protection_status")
    val protectionStatus: ProtectionStatus = ProtectionStatus.ACTIVE,

    @ColumnInfo(name = "last_seen_at")
    val lastSeenAt: Instant? = null,

    @ColumnInfo(name = "paired_at")
    val pairedAt: Instant? = null,

    @ColumnInfo(name = "unpaired_at")
    val unpairedAt: Instant? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null
)
