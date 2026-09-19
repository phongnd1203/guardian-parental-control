package app.guardian.android.feature.family.domain.model

import java.time.Instant

/**
 * Pure domain representation of an active device pairing session.
 */
data class PairingSession(
    val sessionId: String,
    val childId: String,
    val pairingToken: String,
    val manualCode: String,
    val expiresAt: Instant
) {
    val isExpired: Boolean
        get() = Instant.now().isAfter(expiresAt)
}
