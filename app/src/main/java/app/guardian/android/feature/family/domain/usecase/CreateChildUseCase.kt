package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import java.time.LocalDate

class CreateChildUseCase(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(
        familyId: String,
        name: String,
        nickname: String? = null,
        dob: LocalDate? = null,
        avatarBytes: ByteArray? = null
    ): Result<Child> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty() || trimmedName.length > 50) {
            return Result.failure(IllegalArgumentException("Child name must be between 1 and 50 characters"))
        }
        val trimmedNickname = nickname?.trim()
        if (trimmedNickname != null && trimmedNickname.length > 30) {
            return Result.failure(IllegalArgumentException("Nickname must not exceed 30 characters"))
        }
        if (dob != null && dob.isAfter(LocalDate.now())) {
            return Result.failure(IllegalArgumentException("Date of birth cannot be in the future"))
        }

        return repository.createChild(
            familyId = familyId,
            name = trimmedName,
            nickname = trimmedNickname,
            dob = dob,
            avatarBytes = avatarBytes
        )
    }
}
