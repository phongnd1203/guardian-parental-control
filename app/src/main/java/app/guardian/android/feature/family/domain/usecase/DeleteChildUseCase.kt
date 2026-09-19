package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class DeleteChildUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(childId: String): Result<Unit> =
        repository.deleteChild(childId)
}
