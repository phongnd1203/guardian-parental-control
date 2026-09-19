package app.guardian.android.feature.family.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import app.guardian.android.feature.family.data.local.entity.DeviceEntity
import app.guardian.android.feature.family.data.local.entity.DevicePermissionStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices WHERE family_id = :familyId AND unpaired_at IS NULL ORDER BY name ASC")
    fun observeDevicesForFamily(familyId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE child_id = :childId AND unpaired_at IS NULL ORDER BY name ASC")
    fun observeDevicesForChild(childId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :deviceId")
    fun observeDeviceById(deviceId: String): Flow<DeviceEntity?>

    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Upsert
    suspend fun upsertDevices(devices: List<DeviceEntity>)

    @Upsert
    suspend fun upsertDevice(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun deleteDevice(deviceId: String): Int

    @Query("DELETE FROM devices WHERE child_id = :childId")
    suspend fun clearForChild(childId: String): Int

    @Query("DELETE FROM devices WHERE family_id = :familyId")
    suspend fun clearForFamily(familyId: String): Int

    // Permission Status
    @Query("SELECT * FROM device_permission_status WHERE device_id = :deviceId")
    fun observePermissionStatus(deviceId: String): Flow<DevicePermissionStatusEntity?>

    @Query("SELECT * FROM device_permission_status WHERE device_id = :deviceId")
    suspend fun getPermissionStatus(deviceId: String): DevicePermissionStatusEntity?

    @Upsert
    suspend fun upsertPermissionStatus(status: DevicePermissionStatusEntity)

    @Query("DELETE FROM device_permission_status WHERE device_id = :deviceId")
    suspend fun deletePermissionStatus(deviceId: String): Int
}
