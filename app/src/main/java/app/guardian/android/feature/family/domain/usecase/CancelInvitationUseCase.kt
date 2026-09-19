package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class CancelInvitationUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(invitationId: String): Result<Unit> =
        repository.cancelInvitation(invitationId)
}
