package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class RefreshFamilyUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(familyId: String? = null): Result<Unit> =
        repository.refreshFamily(familyId)
}
