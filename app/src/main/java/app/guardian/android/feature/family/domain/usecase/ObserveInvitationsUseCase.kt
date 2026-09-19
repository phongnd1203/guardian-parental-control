package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.FamilyInvitation
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class ObserveInvitationsUseCase(
    private val repository: FamilyRepository
) {
    operator fun invoke(familyId: String): Flow<List<FamilyInvitation>> =
        repository.observeInvitations(familyId)
}
