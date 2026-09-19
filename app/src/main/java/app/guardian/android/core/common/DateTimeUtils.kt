package app.guardian.android.core.common

import app.guardian.android.core.model.DeviceOnlineStatus
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeUtils {

    private val isoFormatter = DateTimeFormatter.ISO_INSTANT
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    /**
     * Parse ISO 8601 string to Instant safely.
     */
    fun parseInstant(isoString: String?): Instant? {
        if (isoString.isNullOrBlank()) return null
        return try {
            Instant.parse(isoString)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Format Instant to ISO 8601 string.
     */
    fun formatInstant(instant: Instant?): String? {
        return instant?.let { isoFormatter.format(it) }
    }

    /**
     * Parse LocalDate from ISO string ("yyyy-MM-dd").
     */
    fun parseLocalDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dateString)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Format LocalDate to "yyyy-MM-dd".
     */
    fun formatLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    /**
     * Calculate age from a LocalDate dateOfBirth.
     */
    fun calculateAge(dob: LocalDate?, now: LocalDate = LocalDate.now()): Int? {
        if (dob == null) return null
        if (dob.isAfter(now)) return 0
        return Period.between(dob, now).years
    }

    /**
     * Format age as a readable string: e.g. "10 years old".
     */
    fun formatAgeText(dob: LocalDate?, now: LocalDate = LocalDate.now()): String {
        val age = calculateAge(dob, now) ?: return ""
        return if (age <= 1) "$age year old" else "$age years old"
    }

    /**
     * Determine device online status based on lastSeenAt timestamp.
     * <= 2 minutes: ONLINE
     * 2-15 minutes: RECENTLY_ONLINE
     * > 15 minutes: OFFLINE
     */
    fun calculateOnlineStatus(lastSeenAt: Instant?, now: Instant = Instant.now()): DeviceOnlineStatus {
        if (lastSeenAt == null) return DeviceOnlineStatus.OFFLINE
        val duration = Duration.between(lastSeenAt, now)
        val minutes = duration.toMinutes()

        return when {
            minutes <= 2 -> DeviceOnlineStatus.ONLINE
            minutes <= 15 -> DeviceOnlineStatus.RECENTLY_ONLINE
            else -> DeviceOnlineStatus.OFFLINE
        }
    }

    /**
     * Format friendly last seen text: e.g., "Online", "5m ago", "Yesterday", etc.
     */
    fun formatLastSeenText(lastSeenAt: Instant?, now: Instant = Instant.now()): String {
        if (lastSeenAt == null) return "Never seen"
        val duration = Duration.between(lastSeenAt, now)
        val minutes = duration.toMinutes()

        return when {
            minutes < 2 -> "Online"
            minutes < 60 -> "${minutes}m ago"
            minutes < 1440 -> "${duration.toHours()}h ago"
            else -> "${duration.toDays()}d ago"
        }
    }
}
