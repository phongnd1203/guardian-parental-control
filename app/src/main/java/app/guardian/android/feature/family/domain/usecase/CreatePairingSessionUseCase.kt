package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.PairingSession
import app.guardian.android.feature.family.domain.repository.FamilyRepository

class CreatePairingSessionUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(childId: String): Result<PairingSession> =
        repository.createPairingSession(childId)
}
