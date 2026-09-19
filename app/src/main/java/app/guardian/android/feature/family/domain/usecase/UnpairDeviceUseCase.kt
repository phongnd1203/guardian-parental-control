package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class UnpairDeviceUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Unit> =
        repository.unpairDevice(deviceId)
}
