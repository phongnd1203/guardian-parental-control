package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class ObserveDevicesUseCase(
    private val repository: FamilyRepository
) {
    operator fun invoke(childId: String): Flow<List<Device>> =
        repository.observeDevices(childId)
}
