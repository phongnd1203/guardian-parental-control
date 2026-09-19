package app.guardian.android.core.database

import app.guardian.android.core.model.ChildStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.InvitationStatus
import app.guardian.android.core.model.ProtectionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `Instant conversion works both ways`() {
        val now = Instant.ofEpochMilli(1726700000000L)
        val timestamp = converters.dateToTimestamp(now)
        assertEquals(1726700000000L, timestamp)

        val restored = converters.fromTimestamp(timestamp)
        assertEquals(now, restored)

        assertNull(converters.dateToTimestamp(null))
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun `LocalDate conversion works both ways`() {
        val date = LocalDate.of(2016, 5, 15)
        val dateStr = converters.localDateToString(date)
        assertEquals("2016-05-15", dateStr)

        val restored = converters.fromLocalDateString(dateStr)
        assertEquals(date, restored)

        assertNull(converters.localDateToString(null))
        assertNull(converters.fromLocalDateString(null))
        assertNull(converters.fromLocalDateString("invalid-date"))
    }

    @Test
    fun `FamilyRole enum conversion works both ways`() {
        for (role in FamilyRole.entries) {
            val str = converters.familyRoleToString(role)
            assertEquals(role.name, str)
            val restored = converters.fromFamilyRole(str)
            assertEquals(role, restored)
        }
        assertNull(converters.familyRoleToString(null))
        assertEquals(FamilyRole.VIEWER, converters.fromFamilyRole("UNKNOWN_ROLE"))
    }

    @Test
    fun `ChildStatus enum conversion works both ways`() {
        for (status in ChildStatus.entries) {
            val str = converters.childStatusToString(status)
            assertEquals(status.name, str)
            val restored = converters.fromChildStatus(str)
            assertEquals(status, restored)
        }
        assertNull(converters.childStatusToString(null))
        assertEquals(ChildStatus.ACTIVE, converters.fromChildStatus("UNKNOWN_STATUS"))
    }

    @Test
    fun `ProtectionStatus enum conversion works both ways`() {
        for (status in ProtectionStatus.entries) {
            val str = converters.protectionStatusToString(status)
            assertEquals(status.name, str)
            val restored = converters.fromProtectionStatus(str)
            assertEquals(status, restored)
        }
        assertNull(converters.protectionStatusToString(null))
        assertEquals(ProtectionStatus.ACTIVE, converters.fromProtectionStatus("UNKNOWN_STATUS"))
    }

    @Test
    fun `InvitationStatus enum conversion works both ways`() {
        for (status in InvitationStatus.entries) {
            val str = converters.invitationStatusToString(status)
            assertEquals(status.name, str)
            val restored = converters.fromInvitationStatus(str)
            assertEquals(status, restored)
        }
        assertNull(converters.invitationStatusToString(null))
        assertEquals(InvitationStatus.PENDING, converters.fromInvitationStatus("UNKNOWN_STATUS"))
    }
}
