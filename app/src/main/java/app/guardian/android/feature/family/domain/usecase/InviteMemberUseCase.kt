package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.core.model.FamilyRole
import app.guardian.android.feature.family.domain.repository.FamilyRepository

class InviteMemberUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(email: String, role: FamilyRole): Result<Unit> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Invalid email address"))
        }
        if (role == FamilyRole.OWNER) {
            return Result.failure(IllegalArgumentException("Cannot invite someone directly with the OWNER role"))
        }

        return repository.inviteMember(trimmedEmail, role)
    }
}
