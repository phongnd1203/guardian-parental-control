package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.repository.FamilyRepository

class AcceptInvitationUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        val trimmed = token.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invitation token cannot be empty"))
        }
        return repository.acceptInvitation(trimmed)
    }
}
