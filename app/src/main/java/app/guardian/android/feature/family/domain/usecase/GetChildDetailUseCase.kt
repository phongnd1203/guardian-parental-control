package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class GetChildDetailUseCase(
    private val repository: FamilyRepository
) {
    operator fun invoke(childId: String): Flow<Child?> =
        repository.observeChild(childId)

    suspend fun getOnce(childId: String): Child? =
        repository.getChild(childId)
}
