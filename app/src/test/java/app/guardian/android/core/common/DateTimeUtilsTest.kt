package app.guardian.android.core.common

import app.guardian.android.core.model.DeviceOnlineStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DateTimeUtilsTest {

    @Test
    fun `calculate age correctly from dateOfBirth`() {
        val now = LocalDate.of(2026, 9, 19)
        val dob = LocalDate.of(2016, 5, 10)
        val age = DateTimeUtils.calculateAge(dob, now)
        assertEquals(10, age)
        assertEquals("10 years old", DateTimeUtils.formatAgeText(dob, now))

        val oneYearOld = LocalDate.of(2025, 9, 10)
        assertEquals("1 year old", DateTimeUtils.formatAgeText(oneYearOld, now))

        assertNull(DateTimeUtils.calculateAge(null, now))
        assertEquals("", DateTimeUtils.formatAgeText(null, now))
    }

    @Test
    fun `calculate online status correctly based on time difference`() {
        val now = Instant.parse("2026-09-19T10:00:00Z")

        // 1 minute ago -> ONLINE
        val oneMinAgo = Instant.parse("2026-09-19T09:59:00Z")
        assertEquals(DeviceOnlineStatus.ONLINE, DateTimeUtils.calculateOnlineStatus(oneMinAgo, now))
        assertEquals("Online", DateTimeUtils.formatLastSeenText(oneMinAgo, now))

        // 5 minutes ago -> RECENTLY_ONLINE
        val fiveMinAgo = Instant.parse("2026-09-19T09:55:00Z")
        assertEquals(DeviceOnlineStatus.RECENTLY_ONLINE, DateTimeUtils.calculateOnlineStatus(fiveMinAgo, now))
        assertEquals("5m ago", DateTimeUtils.formatLastSeenText(fiveMinAgo, now))

        // 30 minutes ago -> OFFLINE
        val thirtyMinAgo = Instant.parse("2026-09-19T09:30:00Z")
        assertEquals(DeviceOnlineStatus.OFFLINE, DateTimeUtils.calculateOnlineStatus(thirtyMinAgo, now))
        assertEquals("30m ago", DateTimeUtils.formatLastSeenText(thirtyMinAgo, now))

        // Null -> OFFLINE
        assertEquals(DeviceOnlineStatus.OFFLINE, DateTimeUtils.calculateOnlineStatus(null, now))
        assertEquals("Never seen", DateTimeUtils.formatLastSeenText(null, now))
    }
}
