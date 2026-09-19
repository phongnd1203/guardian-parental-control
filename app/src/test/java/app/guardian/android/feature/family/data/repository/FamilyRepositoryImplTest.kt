package app.guardian.android.feature.family.data.repository

import app.guardian.android.core.model.ChildStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.InvitationStatus
import app.guardian.android.core.model.ProtectionStatus
import app.guardian.android.feature.family.data.local.dao.ChildDao
import app.guardian.android.feature.family.data.local.dao.DeviceDao
import app.guardian.android.feature.family.data.local.dao.FamilyDao
import app.guardian.android.feature.family.data.local.dao.InvitationDao
import app.guardian.android.feature.family.data.local.dao.MemberDao
import app.guardian.android.feature.family.data.local.entity.ChildEntity
import app.guardian.android.feature.family.data.local.entity.DeviceEntity
import app.guardian.android.feature.family.data.local.entity.DevicePermissionStatusEntity
import app.guardian.android.feature.family.data.local.entity.FamilyEntity
import app.guardian.android.feature.family.data.local.entity.FamilyMemberEntity
import app.guardian.android.feature.family.data.local.entity.InvitationEntity
import app.guardian.android.feature.family.data.remote.FamilyFunctionDataSource
import app.guardian.android.feature.family.data.remote.FamilyRealtimeDataSource
import app.guardian.android.feature.family.data.remote.FamilyRemoteDataSource
import app.guardian.android.feature.family.data.remote.dto.ClaimPairingRequest
import app.guardian.android.feature.family.data.remote.dto.ClaimPairingResponse
import app.guardian.android.feature.family.data.remote.dto.ChildDto
import app.guardian.android.feature.family.data.remote.dto.CreatePairingResponse
import app.guardian.android.feature.family.data.remote.dto.DeviceDto
import app.guardian.android.feature.family.data.remote.dto.DevicePermissionStatusDto
import app.guardian.android.feature.family.data.remote.dto.FamilyDto
import app.guardian.android.feature.family.data.remote.dto.FamilyMemberDto
import app.guardian.android.feature.family.data.remote.dto.InvitationDto
import app.guardian.android.feature.family.data.remote.dto.UnpairDeviceResponse
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class FamilyRepositoryImplTest {

    private lateinit var fakeFamilyDao: FakeFamilyDao
    private lateinit var fakeChildDao: FakeChildDao
    private lateinit var fakeDeviceDao: FakeDeviceDao
    private lateinit var fakeMemberDao: FakeMemberDao
    private lateinit var fakeInvitationDao: FakeInvitationDao
    private lateinit var fakeRemoteDataSource: FakeFamilyRemoteDataSource
    private lateinit var fakeFunctionDataSource: FakeFamilyFunctionDataSource
    private lateinit var fakeRealtimeDataSource: FakeFamilyRealtimeDataSource

    private lateinit var repository: FamilyRepositoryImpl

    @Before
    fun setUp() {
        fakeFamilyDao = FakeFamilyDao()
        fakeChildDao = FakeChildDao()
        fakeDeviceDao = FakeDeviceDao()
        fakeMemberDao = FakeMemberDao()
        fakeInvitationDao = FakeInvitationDao()
        fakeRemoteDataSource = FakeFamilyRemoteDataSource()
        fakeFunctionDataSource = FakeFamilyFunctionDataSource()
        fakeRealtimeDataSource = FakeFamilyRealtimeDataSource()

        repository = FamilyRepositoryImpl(
            familyDao = fakeFamilyDao,
            childDao = fakeChildDao,
            deviceDao = fakeDeviceDao,
            memberDao = fakeMemberDao,
            invitationDao = fakeInvitationDao,
            remoteDataSource = fakeRemoteDataSource,
            functionDataSource = fakeFunctionDataSource,
            realtimeDataSource = fakeRealtimeDataSource,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun `observeFamily emits correctly from FamilyDao`() = runBlocking {
        fakeFamilyDao.upsertFamily(
            FamilyEntity(id = "fam-1", name = "The Robinsons", ownerUserId = "user-1")
        )

        val family = repository.observeFamily().first()
        assertNotNull(family)
        assertEquals("fam-1", family?.id)
        assertEquals("The Robinsons", family?.name)
    }

    @Test
    fun `refreshFamily populates all Room DAOs from RemoteDataSource`() = runBlocking {
        fakeRemoteDataSource.mockFamily = FamilyDto(id = "fam-1", name = "The Incredibles", ownerUserId = "user-1")
        fakeRemoteDataSource.mockChildren = listOf(
            ChildDto(id = "child-1", familyId = "fam-1", name = "Dash", status = "ACTIVE")
        )
        fakeRemoteDataSource.mockMembers = listOf(
            FamilyMemberDto(id = "mem-1", familyId = "fam-1", userId = "user-1", role = "OWNER")
        )
        fakeRemoteDataSource.mockDevices = listOf(
            DeviceDto(id = "dev-1", familyId = "fam-1", childId = "child-1", name = "Dash Phone")
        )
        fakeRemoteDataSource.mockDevicePermissions["dev-1"] = DevicePermissionStatusDto(
            deviceId = "dev-1",
            usageAccess = true,
            accessibilityService = true
        )

        val result = repository.refreshFamily("fam-1")
        assertTrue(result.isSuccess)

        val cachedFamily = fakeFamilyDao.getFamilyById("fam-1")
        assertNotNull(cachedFamily)
        assertEquals("The Incredibles", cachedFamily?.name)

        val cachedChild = fakeChildDao.getChildById("child-1")
        assertNotNull(cachedChild)
        assertEquals("Dash", cachedChild?.name)

        val cachedDevice = fakeDeviceDao.getDeviceById("dev-1")
        assertNotNull(cachedDevice)
        assertEquals("Dash Phone", cachedDevice?.name)

        val cachedPermission = fakeDeviceDao.getPermissionStatus("dev-1")
        assertNotNull(cachedPermission)
        assertEquals(true, cachedPermission?.usageAccess)
    }

    @Test
    fun `createChild inserts into remote, uploads avatar, and saves to Room`() = runBlocking {
        fakeRemoteDataSource.mockUploadedAvatarPath = "fam-1/children/child-100/avatar.webp"

        val avatarBytes = byteArrayOf(1, 2, 3)
        val result = repository.createChild(
            familyId = "fam-1",
            name = "Jack-Jack",
            nickname = "Baby",
            dob = LocalDate.of(2023, 1, 1),
            avatarBytes = avatarBytes
        )

        assertTrue(result.isSuccess)
        val createdChild = result.getOrNull()
        assertNotNull(createdChild)
        assertEquals("Jack-Jack", createdChild?.name)

        val cached = fakeChildDao.getChildById(createdChild!!.id)
        assertNotNull(cached)
        assertEquals("Jack-Jack", cached?.name)
        assertEquals("fam-1/children/child-100/avatar.webp", cached?.avatarPath)
    }

    @Test
    fun `deleteChild calls function and removes child and devices from Room`() = runBlocking {
        fakeChildDao.upsertChild(
            ChildEntity(id = "c-1", familyId = "fam-1", name = "Jack")
        )
        fakeDeviceDao.upsertDevice(
            DeviceEntity(id = "d-1", familyId = "fam-1", childId = "c-1", name = "Tablet")
        )

        val result = repository.deleteChild("c-1")
        assertTrue(result.isSuccess)

        assertEquals(null, fakeChildDao.getChildById("c-1"))
        assertEquals(null, fakeDeviceDao.getDeviceById("d-1"))
    }

    @Test
    fun `createPairingSession returns domain PairingSession`() = runBlocking {
        fakeFunctionDataSource.mockPairingResponse = CreatePairingResponse(
            pairingSessionId = "sess-123",
            pairingToken = "secret-token-xyz",
            manualCode = "123456",
            expiresAt = Instant.now().plusSeconds(600).toString()
        )

        val result = repository.createPairingSession("c-1")
        assertTrue(result.isSuccess)
        val session = result.getOrNull()
        assertNotNull(session)
        assertEquals("sess-123", session?.sessionId)
        assertEquals("123456", session?.manualCode)
        assertEquals("secret-token-xyz", session?.pairingToken)
    }

    @Test
    fun `unpairDevice calls function and cleans device and permission from Room`() = runBlocking {
        fakeDeviceDao.upsertDevice(
            DeviceEntity(id = "dev-10", familyId = "fam-1", childId = "c-1", name = "Phone")
        )
        fakeDeviceDao.upsertPermissionStatus(
            DevicePermissionStatusEntity(deviceId = "dev-10", usageAccess = true)
        )

        val result = repository.unpairDevice("dev-10")
        assertTrue(result.isSuccess)

        assertEquals(null, fakeDeviceDao.getDeviceById("dev-10"))
        assertEquals(null, fakeDeviceDao.getPermissionStatus("dev-10"))
    }

    @Test
    fun `renameDevice updates Room on success`() = runBlocking {
        fakeDeviceDao.upsertDevice(
            DeviceEntity(id = "dev-20", familyId = "fam-1", childId = "c-1", name = "Old Name")
        )

        val result = repository.renameDevice("dev-20", "New Name")
        assertTrue(result.isSuccess)

        val updated = fakeDeviceDao.getDeviceById("dev-20")
        assertEquals("New Name", updated?.name)
    }

    // --- Fakes ---

    class FakeFamilyDao : FamilyDao {
        private val state = MutableStateFlow<FamilyEntity?>(null)
        override fun observeCurrentFamily(): Flow<FamilyEntity?> = state
        override fun observeFamily(familyId: String): Flow<FamilyEntity?> = state
        override suspend fun getFamilyById(familyId: String): FamilyEntity? = state.value
        override suspend fun upsertFamily(family: FamilyEntity) { state.value = family }
        override suspend fun deleteFamily(familyId: String): Int { state.value = null; return 1 }
        override suspend fun clear(): Int { state.value = null; return 1 }
    }

    class FakeChildDao : ChildDao {
        private val children = mutableMapOf<String, ChildEntity>()
        private val flow = MutableStateFlow<List<ChildEntity>>(emptyList())

        override fun observeChildren(familyId: String): Flow<List<ChildEntity>> = flow
        override fun observeChildById(childId: String): Flow<ChildEntity?> =
            MutableStateFlow(children[childId])
        override suspend fun getChildById(childId: String): ChildEntity? = children[childId]
        override suspend fun upsertChildren(children: List<ChildEntity>) {
            children.forEach { this.children[it.id] = it }
            flow.value = this.children.values.toList()
        }
        override suspend fun upsertChild(child: ChildEntity) {
            children[child.id] = child
            flow.value = children.values.toList()
        }
        override suspend fun deleteChild(childId: String): Int {
            children.remove(childId)
            flow.value = children.values.toList()
            return 1
        }
        override suspend fun clearForFamily(familyId: String): Int {
            children.clear()
            flow.value = emptyList()
            return 1
        }
    }

    class FakeDeviceDao : DeviceDao {
        private val devices = mutableMapOf<String, DeviceEntity>()
        private val permissions = mutableMapOf<String, DevicePermissionStatusEntity>()
        private val devicesFlow = MutableStateFlow<List<DeviceEntity>>(emptyList())

        override fun observeDevicesForFamily(familyId: String): Flow<List<DeviceEntity>> = devicesFlow
        override fun observeDevicesForChild(childId: String): Flow<List<DeviceEntity>> = devicesFlow
        override fun observeDeviceById(deviceId: String): Flow<DeviceEntity?> =
            MutableStateFlow(devices[deviceId])
        override suspend fun getDeviceById(deviceId: String): DeviceEntity? = devices[deviceId]
        override suspend fun upsertDevices(devices: List<DeviceEntity>) {
            devices.forEach { this.devices[it.id] = it }
            devicesFlow.value = this.devices.values.toList()
        }
        override suspend fun upsertDevice(device: DeviceEntity) {
            devices[device.id] = device
            devicesFlow.value = devices.values.toList()
        }
        override suspend fun deleteDevice(deviceId: String): Int {
            devices.remove(deviceId)
            devicesFlow.value = devices.values.toList()
            return 1
        }
        override suspend fun clearForChild(childId: String): Int {
            devices.values.removeAll { it.childId == childId }
            devicesFlow.value = devices.values.toList()
            return 1
        }
        override suspend fun clearForFamily(familyId: String): Int {
            devices.clear()
            devicesFlow.value = emptyList()
            return 1
        }
        override fun observePermissionStatus(deviceId: String): Flow<DevicePermissionStatusEntity?> =
            MutableStateFlow(permissions[deviceId])
        override suspend fun getPermissionStatus(deviceId: String): DevicePermissionStatusEntity? =
            permissions[deviceId]
        override suspend fun upsertPermissionStatus(status: DevicePermissionStatusEntity) {
            permissions[status.deviceId] = status
        }
        override suspend fun deletePermissionStatus(deviceId: String): Int {
            permissions.remove(deviceId)
            return 1
        }
    }

    class FakeMemberDao : MemberDao {
        private val members = mutableMapOf<String, FamilyMemberEntity>()
        private val flow = MutableStateFlow<List<FamilyMemberEntity>>(emptyList())
        override fun observeMembers(familyId: String): Flow<List<FamilyMemberEntity>> = flow
        override suspend fun getMemberById(memberId: String): FamilyMemberEntity? = members[memberId]
        override suspend fun upsertMembers(members: List<FamilyMemberEntity>) {
            members.forEach { this.members[it.id] = it }
            flow.value = this.members.values.toList()
        }
        override suspend fun upsertMember(member: FamilyMemberEntity) {
            members[member.id] = member
            flow.value = members.values.toList()
        }
        override suspend fun deleteMember(memberId: String): Int {
            members.remove(memberId)
            flow.value = members.values.toList()
            return 1
        }
        override suspend fun clearForFamily(familyId: String): Int {
            members.clear()
            flow.value = emptyList()
            return 1
        }
    }

    class FakeInvitationDao : InvitationDao {
        private val invitations = mutableMapOf<String, InvitationEntity>()
        private val flow = MutableStateFlow<List<InvitationEntity>>(emptyList())
        override fun observePendingInvitations(familyId: String): Flow<List<InvitationEntity>> = flow
        override suspend fun getInvitationById(invitationId: String): InvitationEntity? = invitations[invitationId]
        override suspend fun upsertInvitations(invitations: List<InvitationEntity>) {
            invitations.forEach { this.invitations[it.id] = it }
            flow.value = this.invitations.values.toList()
        }
        override suspend fun upsertInvitation(invitation: InvitationEntity) {
            invitations[invitation.id] = invitation
            flow.value = invitations.values.toList()
        }
        override suspend fun deleteInvitation(invitationId: String): Int {
            invitations.remove(invitationId)
            flow.value = invitations.values.toList()
            return 1
        }
        override suspend fun clearForFamily(familyId: String): Int {
            invitations.clear()
            flow.value = emptyList()
            return 1
        }
    }

    class FakeFamilyRemoteDataSource : FamilyRemoteDataSource {
        var mockFamily: FamilyDto? = null
        var mockChildren: List<ChildDto> = emptyList()
        var mockMembers: List<FamilyMemberDto> = emptyList()
        var mockDevices: List<DeviceDto> = emptyList()
        val mockDevicePermissions = mutableMapOf<String, DevicePermissionStatusDto>()
        var mockInvitations: List<InvitationDto> = emptyList()
        var mockUploadedAvatarPath = "family-avatars/test/avatar.webp"

        override suspend fun getMyFamily(): FamilyDto? = mockFamily
        override suspend fun getChildren(familyId: String): List<ChildDto> = mockChildren
        override suspend fun getMembers(familyId: String): List<FamilyMemberDto> = mockMembers
        override suspend fun getDevices(familyId: String): List<DeviceDto> = mockDevices
        override suspend fun getDevicePermissionStatus(deviceId: String): DevicePermissionStatusDto? =
            mockDevicePermissions[deviceId]
        override suspend fun getPendingInvitations(familyId: String): List<InvitationDto> = mockInvitations
        override suspend fun updateChildInfo(
            childId: String,
            name: String,
            nickname: String?,
            dob: String?,
            avatarPath: String?
        ): Result<Unit> = Result.success(Unit)

        override suspend fun createChild(
            familyId: String,
            name: String,
            nickname: String?,
            dob: String?,
            avatarPath: String?
        ): Result<ChildDto> = Result.success(
            ChildDto(
                id = "child-generated-id",
                familyId = familyId,
                name = name,
                nickname = nickname,
                dateOfBirth = dob,
                avatarPath = avatarPath
            )
        )

        override suspend fun uploadChildAvatar(
            familyId: String,
            childId: String,
            bytes: ByteArray
        ): Result<String> = Result.success(mockUploadedAvatarPath)

        override suspend fun renameDevice(deviceId: String, newName: String): Result<Unit> = Result.success(Unit)
    }

    class FakeFamilyFunctionDataSource : FamilyFunctionDataSource {
        var mockPairingResponse: CreatePairingResponse? = null
        override suspend fun createFamily(name: String): Result<String> = Result.success("fam-created")
        override suspend fun inviteMember(email: String, role: String): Result<Unit> = Result.success(Unit)
        override suspend fun acceptInvite(token: String): Result<Unit> = Result.success(Unit)
        override suspend fun cancelInvite(invitationId: String): Result<Unit> = Result.success(Unit)
        override suspend fun changeMemberRole(memberId: String, newRole: String): Result<Unit> = Result.success(Unit)
        override suspend fun removeMember(memberId: String): Result<Unit> = Result.success(Unit)
        override suspend fun transferOwnership(newOwnerMemberId: String): Result<Unit> = Result.success(Unit)
        override suspend fun createPairing(childId: String): Result<CreatePairingResponse> =
            Result.success(mockPairingResponse ?: CreatePairingResponse("sess", "tok", "123456", Instant.now().toString()))
        override suspend fun claimPairing(request: ClaimPairingRequest): Result<ClaimPairingResponse> =
            Result.success(ClaimPairingResponse("dev", "child", "tok", "claimed"))
        override suspend fun unpairDevice(deviceId: String): Result<UnpairDeviceResponse> =
            Result.success(UnpairDeviceResponse(success = true, unpairedAt = Instant.now().toString()))
        override suspend fun deleteChild(childId: String): Result<Unit> = Result.success(Unit)
        override suspend fun deleteFamily(familyId: String): Result<Unit> = Result.success(Unit)
    }

    class FakeFamilyRealtimeDataSource : FamilyRealtimeDataSource {
        override suspend fun subscribeFamilyChanges(familyId: String): Flow<PostgresAction> = emptyFlow()
    }
}
