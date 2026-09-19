package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class GetDeviceDetailUseCase(
    private val repository: FamilyRepository
) {
    operator fun invoke(deviceId: String): Flow<Device?> =
        repository.observeDevice(deviceId)

    suspend fun getOnce(deviceId: String): Device? =
        repository.getDevice(deviceId)
}
