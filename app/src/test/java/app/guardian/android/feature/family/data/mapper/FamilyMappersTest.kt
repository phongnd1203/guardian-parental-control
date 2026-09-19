package app.guardian.android.feature.family.data.mapper

import app.guardian.android.core.model.ChildStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.ProtectionStatus
import app.guardian.android.feature.family.data.local.entity.ChildEntity
import app.guardian.android.feature.family.data.local.entity.DeviceEntity
import app.guardian.android.feature.family.data.local.entity.FamilyEntity
import app.guardian.android.feature.family.data.remote.dto.ChildDto
import app.guardian.android.feature.family.data.remote.dto.DeviceDto
import app.guardian.android.feature.family.data.remote.dto.FamilyDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class FamilyMappersTest {

    @Test
    fun `map FamilyDto to Entity and Domain`() {
        val dto = FamilyDto(
            id = "f-123",
            name = "Nguyen Family",
            ownerUserId = "u-456",
            createdAt = "2026-09-19T01:00:00Z",
            updatedAt = "2026-09-19T01:00:00Z"
        )
        val entity = dto.toEntity()
        assertEquals("f-123", entity.id)
        assertEquals("Nguyen Family", entity.name)
        assertEquals("u-456", entity.ownerUserId)
        assertNotNull(entity.createdAt)

        val domain = entity.toDomain()
        assertEquals("f-123", domain.id)
        assertEquals("Nguyen Family", domain.name)
    }

    @Test
    fun `map ChildDto to Entity and Domain with calculated age`() {
        val dto = ChildDto(
            id = "c-123",
            familyId = "f-123",
            name = "Nam Nguyen",
            nickname = "Nam",
            dateOfBirth = "2016-05-15",
            status = "ACTIVE"
        )
        val entity = dto.toEntity()
        assertEquals("c-123", entity.id)
        assertEquals(ChildStatus.ACTIVE, entity.status)
        assertEquals(LocalDate.of(2016, 5, 15), entity.dateOfBirth)

        val domain = entity.toDomain()
        assertEquals("Nam", domain.displayName)
        assertNotNull(domain.age)
    }

    @Test
    fun `map DeviceDto to Entity and Domain`() {
        val dto = DeviceDto(
            id = "d-123",
            familyId = "f-123",
            childId = "c-123",
            name = "Nam's Galaxy",
            manufacturer = "Samsung",
            model = "Galaxy S25",
            batteryLevel = 85,
            charging = true,
            protectionStatus = "ACTIVE",
            lastSeenAt = "2026-09-19T08:00:00Z"
        )
        val entity = dto.toEntity()
        assertEquals(ProtectionStatus.ACTIVE, entity.protectionStatus)
        assertEquals(true, entity.charging)

        val domain = entity.toDomain()
        assertEquals("Samsung Galaxy S25", domain.hardwareDescription)
        assertEquals(85, domain.batteryLevel)
    }
}
