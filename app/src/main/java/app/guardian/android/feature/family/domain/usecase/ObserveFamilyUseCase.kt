package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.Family
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class ObserveFamilyUseCase(
    private val repository: FamilyRepository
) {
    operator fun invoke(): Flow<Family?> = repository.observeFamily()
}
