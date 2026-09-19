package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.core.model.FamilyRole
import app.guardian.android.feature.family.domain.repository.FamilyRepository

class UpdateMemberRoleUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(memberId: String, newRole: FamilyRole): Result<Unit> {
        if (newRole == FamilyRole.OWNER) {
            return Result.failure(IllegalArgumentException("Cannot assign OWNER role via role update; use transfer ownership"))
        }
        return repository.updateMemberRole(memberId, newRole)
    }
}
