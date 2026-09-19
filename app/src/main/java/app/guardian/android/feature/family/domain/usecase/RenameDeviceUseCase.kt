package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class RenameDeviceUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(deviceId: String, newName: String): Result<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("Device name must be between 1 and 50 characters"))
        }
        return repository.renameDevice(deviceId, trimmed)
    }
}
