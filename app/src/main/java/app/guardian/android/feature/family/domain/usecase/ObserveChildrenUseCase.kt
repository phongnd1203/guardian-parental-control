package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class ObserveChildrenUseCase(
    private val repository: FamilyRepository
) {
    operator fun invoke(familyId: String): Flow<List<Child>> =
        repository.observeChildren(familyId)
}
