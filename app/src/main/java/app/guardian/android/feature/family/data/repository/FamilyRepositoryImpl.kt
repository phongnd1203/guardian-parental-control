package app.guardian.android.feature.family.data.repository

import app.guardian.android.core.model.FamilyRole
import app.guardian.android.feature.family.data.local.dao.ChildDao
import app.guardian.android.feature.family.data.local.dao.DeviceDao
import app.guardian.android.feature.family.data.local.dao.FamilyDao
import app.guardian.android.feature.family.data.local.dao.InvitationDao
import app.guardian.android.feature.family.data.local.dao.MemberDao
import app.guardian.android.feature.family.data.mapper.toDomain
import app.guardian.android.feature.family.data.mapper.toEntity
import app.guardian.android.feature.family.data.remote.FamilyFunctionDataSource
import app.guardian.android.feature.family.data.remote.FamilyRealtimeDataSource
import app.guardian.android.feature.family.data.remote.FamilyRemoteDataSource
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.model.Family
import app.guardian.android.feature.family.domain.model.FamilyInvitation
import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.model.PairingSession
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Offline-First implementation of [FamilyRepository].
 *
 * - Room Local DB is the single source of truth for UI observation.
 * - Refresh operations populate Room from Supabase Remote APIs.
 * - Realtime subscriptions push live updates directly into Room.
 * - Critical mutating operations write to Supabase and immediately update local cache on success.
 */
class FamilyRepositoryImpl(
    private val familyDao: FamilyDao,
    private val childDao: ChildDao,
    private val deviceDao: DeviceDao,
    private val memberDao: MemberDao,
    private val invitationDao: InvitationDao,
    private val remoteDataSource: FamilyRemoteDataSource,
    private val functionDataSource: FamilyFunctionDataSource,
    private val realtimeDataSource: FamilyRealtimeDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FamilyRepository {

    private var realtimeJob: Job? = null
    private var activeRealtimeFamilyId: String? = null

    /**
     * Connects to Supabase Realtime for the given [familyId] and refreshes local Room cache on events.
     */
    fun startRealtimeSync(scope: CoroutineScope, familyId: String) {
        if (activeRealtimeFamilyId == familyId && realtimeJob?.isActive == true) return
        realtimeJob?.cancel()
        activeRealtimeFamilyId = familyId

        realtimeJob = scope.launch(ioDispatcher) {
            realtimeDataSource.subscribeFamilyChanges(familyId).collect {
                refreshFamily(familyId)
            }
        }
    }

    fun stopRealtimeSync() {
        realtimeJob?.cancel()
        realtimeJob = null
        activeRealtimeFamilyId = null
    }

    override fun observeFamily(): Flow<Family?> {
        return familyDao.observeCurrentFamily()
            .distinctUntilChanged()
            .map { it?.toDomain() }
    }

    override fun observeChildren(familyId: String): Flow<List<Child>> {
        return childDao.observeChildren(familyId)
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeChild(childId: String): Flow<Child?> {
        return childDao.observeChildById(childId)
            .distinctUntilChanged()
            .map { it?.toDomain() }
    }

    override suspend fun getChild(childId: String): Child? = withContext(ioDispatcher) {
        childDao.getChildById(childId)?.toDomain()
    }

    override fun observeMembers(familyId: String): Flow<List<FamilyMember>> {
        return memberDao.observeMembers(familyId)
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeInvitations(familyId: String): Flow<List<FamilyInvitation>> {
        return invitationDao.observePendingInvitations(familyId)
            .distinctUntilChanged()
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeDevices(childId: String): Flow<List<Device>> {
        return deviceDao.observeDevicesForChild(childId)
            .distinctUntilChanged()
            .map { deviceEntities ->
                deviceEntities.map { entity ->
                    val permission = deviceDao.getPermissionStatus(entity.id)?.toDomain()
                    entity.toDomain(permission)
                }
            }
    }

    override fun observeDevice(deviceId: String): Flow<Device?> {
        return deviceDao.observeDeviceById(deviceId)
            .distinctUntilChanged()
            .map { entity ->
                if (entity == null) null
                else {
                    val permission = deviceDao.getPermissionStatus(entity.id)?.toDomain()
                    entity.toDomain(permission)
                }
            }
    }

    override suspend fun getDevice(deviceId: String): Device? = withContext(ioDispatcher) {
        val entity = deviceDao.getDeviceById(deviceId) ?: return@withContext null
        val permission = deviceDao.getPermissionStatus(deviceId)?.toDomain()
        entity.toDomain(permission)
    }

    override suspend fun refreshFamily(familyId: String?): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val familyDto = remoteDataSource.getMyFamily()
            if (familyDto != null) {
                familyDao.upsertFamily(familyDto.toEntity())
                val targetFamilyId = familyId ?: familyDto.id

                // 1. Refresh children
                val childrenDto = remoteDataSource.getChildren(targetFamilyId)
                childDao.upsertChildren(childrenDto.map { it.toEntity() })

                // 2. Refresh members
                val membersDto = remoteDataSource.getMembers(targetFamilyId)
                memberDao.upsertMembers(membersDto.map { it.toEntity() })

                // 3. Refresh devices & permissions
                val devicesDto = remoteDataSource.getDevices(targetFamilyId)
                deviceDao.upsertDevices(devicesDto.map { it.toEntity() })
                for (device in devicesDto) {
                    val permDto = remoteDataSource.getDevicePermissionStatus(device.id)
                    if (permDto != null) {
                        deviceDao.upsertPermissionStatus(permDto.toEntity())
                    }
                }

                // 4. Refresh invitations
                val invitationsDto = remoteDataSource.getPendingInvitations(targetFamilyId)
                invitationDao.upsertInvitations(invitationsDto.map { it.toEntity() })
            }
            Unit
        }
    }

    override suspend fun createFamily(name: String): Result<String> = withContext(ioDispatcher) {
        functionDataSource.createFamily(name).onSuccess {
            refreshFamily()
        }
    }

    override suspend fun createChild(
        familyId: String,
        name: String,
        nickname: String?,
        dob: LocalDate?,
        avatarBytes: ByteArray?
    ): Result<Child> = withContext(ioDispatcher) {
        runCatching {
            // Step 1: Insert child record via PostgREST
            val createdDto = remoteDataSource.createChild(
                familyId = familyId,
                name = name,
                nickname = nickname,
                dob = dob?.toString(),
                avatarPath = null
            ).getOrThrow()

            var avatarPath: String? = null
            if (avatarBytes != null && avatarBytes.isNotEmpty()) {
                val uploadResult = remoteDataSource.uploadChildAvatar(
                    familyId = familyId,
                    childId = createdDto.id,
                    bytes = avatarBytes
                )
                if (uploadResult.isSuccess) {
                    avatarPath = uploadResult.getOrNull()
                    remoteDataSource.updateChildInfo(
                        childId = createdDto.id,
                        name = name,
                        nickname = nickname,
                        dob = dob?.toString(),
                        avatarPath = avatarPath
                    )
                }
            }

            val finalEntity = createdDto.copy(avatarPath = avatarPath).toEntity()
            childDao.upsertChild(finalEntity)
            finalEntity.toDomain()
        }
    }

    override suspend fun updateChild(
        childId: String,
        name: String,
        nickname: String?,
        dob: LocalDate?,
        avatarBytes: ByteArray?
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            var avatarPath: String? = null
            if (avatarBytes != null && avatarBytes.isNotEmpty()) {
                val existingChild = childDao.getChildById(childId)
                val familyId = existingChild?.familyId
                if (familyId != null) {
                    val uploadResult = remoteDataSource.uploadChildAvatar(
                        familyId = familyId,
                        childId = childId,
                        bytes = avatarBytes
                    )
                    avatarPath = uploadResult.getOrNull()
                }
            }

            remoteDataSource.updateChildInfo(
                childId = childId,
                name = name,
                nickname = nickname,
                dob = dob?.toString(),
                avatarPath = avatarPath
            ).getOrThrow()

            // Update local cache
            val existing = childDao.getChildById(childId)
            if (existing != null) {
                childDao.upsertChild(
                    existing.copy(
                        name = name,
                        nickname = nickname,
                        dateOfBirth = dob,
                        avatarPath = avatarPath ?: existing.avatarPath
                    )
                )
            }
            Unit
        }
    }

    override suspend fun deleteChild(childId: String): Result<Unit> = withContext(ioDispatcher) {
        functionDataSource.deleteChild(childId).onSuccess {
            childDao.deleteChild(childId)
            deviceDao.clearForChild(childId)
        }
    }

    override suspend fun createPairingSession(childId: String): Result<PairingSession> = withContext(ioDispatcher) {
        functionDataSource.createPairing(childId).map { response ->
            response.toDomain(childId)
        }
    }

    override suspend fun unpairDevice(deviceId: String): Result<Unit> = withContext(ioDispatcher) {
        functionDataSource.unpairDevice(deviceId).map {
            deviceDao.deleteDevice(deviceId)
            deviceDao.deletePermissionStatus(deviceId)
            Unit
        }
    }

    override suspend fun renameDevice(deviceId: String, newName: String): Result<Unit> = withContext(ioDispatcher) {
        remoteDataSource.renameDevice(deviceId, newName).onSuccess {
            val existing = deviceDao.getDeviceById(deviceId)
            if (existing != null) {
                deviceDao.upsertDevice(existing.copy(name = newName))
            }
        }
    }

    override suspend fun inviteMember(email: String, role: FamilyRole): Result<Unit> = withContext(ioDispatcher) {
        functionDataSource.inviteMember(email, role.name).onSuccess {
            val familyDto = remoteDataSource.getMyFamily()
            if (familyDto != null) {
                val invitations = remoteDataSource.getPendingInvitations(familyDto.id)
                invitationDao.upsertInvitations(invitations.map { it.toEntity() })
            }
        }
    }

    override suspend fun cancelInvitation(invitationId: String): Result<Unit> = withContext(ioDispatcher) {
        functionDataSource.cancelInvite(invitationId).onSuccess {
            invitationDao.deleteInvitation(invitationId)
        }
    }

    override suspend fun acceptInvitation(token: String): Result<Unit> = withContext(ioDispatcher) {
        functionDataSource.acceptInvite(token).onSuccess {
            refreshFamily()
        }
    }

    override suspend fun updateMemberRole(memberId: String, newRole: FamilyRole): Result<Unit> = withContext(ioDispatcher) {
        functionDataSource.changeMemberRole(memberId, newRole.name).onSuccess {
            val existing = memberDao.getMemberById(memberId)
            if (existing != null) {
                memberDao.upsertMember(existing.copy(role = newRole))
            }
        }
    }

    override suspend fun removeMember(memberId: String): Result<Unit> = withContext(ioDispatcher) {
        functionDataSource.removeMember(memberId).onSuccess {
            memberDao.deleteMember(memberId)
        }
    }
}
