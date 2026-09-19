package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class CreateFamilyUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(name: String): Result<String> {
        val trimmed = name.trim()
        if (trimmed.length < 2 || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("Family name must be between 2 and 50 characters"))
        }
        return repository.createFamily(trimmed)
    }
}
