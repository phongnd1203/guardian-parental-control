package app.guardian.android.feature.family.domain.usecase

import app.guardian.android.core.model.ChildStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.ProtectionStatus
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.model.Family
import app.guardian.android.feature.family.domain.model.FamilyInvitation
import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.model.PairingSession
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class FamilyUseCasesTest {

    private lateinit var fakeRepository: FakeFamilyRepository

    @Before
    fun setUp() {
        fakeRepository = FakeFamilyRepository()
    }

    @Test
    fun `CreateFamilyUseCase validates name length boundaries`() = runBlocking {
        val useCase = CreateFamilyUseCase(fakeRepository)

        val tooShort = useCase("A")
        assertFalse(tooShort.isSuccess)
        assertTrue(tooShort.exceptionOrNull() is IllegalArgumentException)

        val tooLong = useCase("A".repeat(51))
        assertFalse(tooLong.isSuccess)
        assertTrue(tooLong.exceptionOrNull() is IllegalArgumentException)

        val valid = useCase("Smith Family")
        assertTrue(valid.isSuccess)
        assertEquals("fam-created", valid.getOrNull())
    }

    @Test
    fun `CreateChildUseCase validates name, nickname, and dob`() = runBlocking {
        val useCase = CreateChildUseCase(fakeRepository)

        val emptyName = useCase(familyId = "f1", name = "   ")
        assertFalse(emptyName.isSuccess)

        val longNickname = useCase(familyId = "f1", name = "Tommy", nickname = "A".repeat(31))
        assertFalse(longNickname.isSuccess)

        val futureDob = useCase(familyId = "f1", name = "Tommy", dob = LocalDate.now().plusDays(1))
        assertFalse(futureDob.isSuccess)

        val valid = useCase(familyId = "f1", name = "Tommy", nickname = "Tom", dob = LocalDate.of(2015, 5, 20))
        assertTrue(valid.isSuccess)
        assertEquals("Tommy", valid.getOrNull()?.name)
    }

    @Test
    fun `InviteMemberUseCase validates email and blocks OWNER role invitation`() = runBlocking {
        val useCase = InviteMemberUseCase(fakeRepository)

        val invalidEmail = useCase(email = "notanemail", role = FamilyRole.PARENT)
        assertFalse(invalidEmail.isSuccess)

        val ownerRole = useCase(email = "mom@example.com", role = FamilyRole.OWNER)
        assertFalse(ownerRole.isSuccess)

        val valid = useCase(email = "mom@example.com", role = FamilyRole.PARENT)
        assertTrue(valid.isSuccess)
    }

    @Test
    fun `UpdateMemberRoleUseCase blocks setting role to OWNER directly`() = runBlocking {
        val useCase = UpdateMemberRoleUseCase(fakeRepository)

        val resultOwner = useCase(memberId = "m1", newRole = FamilyRole.OWNER)
        assertFalse(resultOwner.isSuccess)

        val resultViewer = useCase(memberId = "m1", newRole = FamilyRole.VIEWER)
        assertTrue(resultViewer.isSuccess)
    }

    @Test
    fun `RenameDeviceUseCase validates device name length`() = runBlocking {
        val useCase = RenameDeviceUseCase(fakeRepository)

        val emptyName = useCase(deviceId = "d1", newName = " ")
        assertFalse(emptyName.isSuccess)

        val tooLong = useCase(deviceId = "d1", newName = "D".repeat(51))
        assertFalse(tooLong.isSuccess)

        val valid = useCase(deviceId = "d1", newName = "Kid Tablet")
        assertTrue(valid.isSuccess)
    }

    // --- Fake FamilyRepository for testing UseCases ---

    class FakeFamilyRepository : FamilyRepository {
        var lastCreatedChildName: String? = null
        var hasFamilyValue: Boolean = true

        override fun observeFamily(): Flow<Family?> = flowOf(Family("f1", "Smiths", "u1"))
        override fun observeChildren(familyId: String): Flow<List<Child>> = flowOf(emptyList())
        override fun observeChild(childId: String): Flow<Child?> = flowOf(null)
        override suspend fun getChild(childId: String): Child? = null
        override fun observeMembers(familyId: String): Flow<List<FamilyMember>> = flowOf(emptyList())
        override fun observeInvitations(familyId: String): Flow<List<FamilyInvitation>> = flowOf(emptyList())
        override fun observeDevices(childId: String): Flow<List<Device>> = flowOf(emptyList())
        override fun observeDevice(deviceId: String): Flow<Device?> = flowOf(null)
        override suspend fun getDevice(deviceId: String): Device? = null
        override suspend fun refreshFamily(familyId: String?): Result<Unit> = Result.success(Unit)
        override suspend fun checkHasFamily(): Boolean = hasFamilyValue
        override suspend fun createFamily(name: String): Result<String> = Result.success("fam-created")

        override suspend fun createChild(
            familyId: String,
            name: String,
            nickname: String?,
            dob: LocalDate?,
            avatarBytes: ByteArray?
        ): Result<Child> {
            lastCreatedChildName = name
            return Result.success(
                Child(
                    id = "c-1",
                    familyId = familyId,
                    name = name,
                    nickname = nickname,
                    dateOfBirth = dob
                )
            )
        }

        override suspend fun updateChild(
            childId: String,
            name: String,
            nickname: String?,
            dob: LocalDate?,
            avatarBytes: ByteArray?
        ): Result<Unit> = Result.success(Unit)

        override suspend fun deleteChild(childId: String): Result<Unit> = Result.success(Unit)
        override suspend fun createPairingSession(childId: String): Result<PairingSession> =
            Result.success(PairingSession("s1", childId, "token", "123456", Instant.now().plusSeconds(600)))
        override suspend fun unpairDevice(deviceId: String): Result<Unit> = Result.success(Unit)
        override suspend fun renameDevice(deviceId: String, newName: String): Result<Unit> = Result.success(Unit)
        override suspend fun inviteMember(email: String, role: FamilyRole): Result<Unit> = Result.success(Unit)
        override suspend fun cancelInvitation(invitationId: String): Result<Unit> = Result.success(Unit)
        override suspend fun acceptInvitation(token: String): Result<Unit> = Result.success(Unit)
        override suspend fun updateMemberRole(memberId: String, newRole: FamilyRole): Result<Unit> = Result.success(Unit)
        override suspend fun removeMember(memberId: String): Result<Unit> = Result.success(Unit)
    }
}
