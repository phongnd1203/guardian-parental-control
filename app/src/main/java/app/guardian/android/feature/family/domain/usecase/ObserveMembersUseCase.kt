package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class ObserveMembersUseCase(
    private val repository: FamilyRepository
) {
    operator fun invoke(familyId: String): Flow<List<FamilyMember>> =
        repository.observeMembers(familyId)
}
