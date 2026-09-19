package app.guardian.android.feature.family.domain.model

import java.time.Instant

/**
 * Pure domain representation of a Family.
 */
data class Family(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
