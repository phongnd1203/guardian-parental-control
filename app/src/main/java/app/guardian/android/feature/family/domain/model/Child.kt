package app.guardian.android.feature.family.domain.model

import app.guardian.android.core.common.DateTimeUtils
import app.guardian.android.core.model.ChildStatus
import java.time.Instant
import java.time.LocalDate

/**
 * Pure domain representation of a Child profile.
 */
data class Child(
    val id: String,
    val familyId: String,
    val name: String,
    val nickname: String? = null,
    val dateOfBirth: LocalDate? = null,
    val avatarPath: String? = null,
    val status: ChildStatus = ChildStatus.ACTIVE,
    val createdBy: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {
    /**
     * Calculated age in years, or null if dateOfBirth is not set.
     */
    val age: Int?
        get() = DateTimeUtils.calculateAge(dateOfBirth)

    /**
     * Readable age description (e.g. "10 years old").
     */
    val ageText: String
        get() = DateTimeUtils.formatAgeText(dateOfBirth)

    /**
     * Preferred display name: nickname if available, else primary name.
     */
    val displayName: String
        get() = nickname?.takeIf { it.isNotBlank() } ?: name
}
