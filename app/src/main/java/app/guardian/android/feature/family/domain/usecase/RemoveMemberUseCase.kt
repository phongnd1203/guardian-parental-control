package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class RemoveMemberUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(memberId: String): Result<Unit> =
        repository.removeMember(memberId)
}
