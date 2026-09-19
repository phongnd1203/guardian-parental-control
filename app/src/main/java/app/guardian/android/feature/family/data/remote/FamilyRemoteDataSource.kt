package app.guardian.android.feature.family.data.remote

import app.guardian.android.core.network.SupabaseClientProvider
import app.guardian.android.feature.family.data.remote.dto.ChildDto
import app.guardian.android.feature.family.data.remote.dto.CreateChildRequestDto
import app.guardian.android.feature.family.data.remote.dto.DeviceDto
import app.guardian.android.feature.family.data.remote.dto.DevicePermissionStatusDto
import app.guardian.android.feature.family.data.remote.dto.FamilyDto
import app.guardian.android.feature.family.data.remote.dto.FamilyMemberDto
import app.guardian.android.feature.family.data.remote.dto.InvitationDto
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FamilyRemoteDataSource {
    suspend fun getMyFamily(): FamilyDto?
    suspend fun getChildren(familyId: String): List<ChildDto>
    suspend fun getMembers(familyId: String): List<FamilyMemberDto>
    suspend fun getDevices(familyId: String): List<DeviceDto>
    suspend fun getDevicePermissionStatus(deviceId: String): DevicePermissionStatusDto?
    suspend fun getPendingInvitations(familyId: String): List<InvitationDto>
    suspend fun updateChildInfo(
        childId: String,
        name: String,
        nickname: String?,
        dob: String?,
        avatarPath: String? = null
    ): Result<Unit>
    suspend fun createChild(
        familyId: String,
        name: String,
        nickname: String?,
        dob: String?,
        avatarPath: String?
    ): Result<ChildDto>
    suspend fun uploadChildAvatar(
        familyId: String,
        childId: String,
        bytes: ByteArray
    ): Result<String>
    suspend fun renameDevice(deviceId: String, newName: String): Result<Unit>
}

class SupabaseFamilyRemoteDataSource : FamilyRemoteDataSource {

    private val postgrest = SupabaseClientProvider.postgrest
    private val storage = SupabaseClientProvider.storage

    override suspend fun getMyFamily(): FamilyDto? = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("families")
                .select {
                    filter {
                        filter("deleted_at", FilterOperator.IS, null)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<FamilyDto>()
        }.getOrNull()
    }

    override suspend fun getChildren(familyId: String): List<ChildDto> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("children")
                .select {
                    filter {
                        eq("family_id", familyId)
                        filter("deleted_at", FilterOperator.IS, null)
                    }
                }
                .decodeList<ChildDto>()
        }.getOrDefault(emptyList())
    }

    override suspend fun getMembers(familyId: String): List<FamilyMemberDto> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("family_members")
                .select {
                    filter {
                        eq("family_id", familyId)
                    }
                }
                .decodeList<FamilyMemberDto>()
        }.getOrDefault(emptyList())
    }

    override suspend fun getDevices(familyId: String): List<DeviceDto> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("devices")
                .select {
                    filter {
                        eq("family_id", familyId)
                        filter("unpaired_at", FilterOperator.IS, null)
                    }
                }
                .decodeList<DeviceDto>()
        }.getOrDefault(emptyList())
    }

    override suspend fun getDevicePermissionStatus(deviceId: String): DevicePermissionStatusDto? = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("device_permission_status")
                .select {
                    filter {
                        eq("device_id", deviceId)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<DevicePermissionStatusDto>()
        }.getOrNull()
    }

    override suspend fun getPendingInvitations(familyId: String): List<InvitationDto> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("family_invitations")
                .select {
                    filter {
                        eq("family_id", familyId)
                        eq("status", "PENDING")
                    }
                }
                .decodeList<InvitationDto>()
        }.getOrDefault(emptyList())
    }

    override suspend fun updateChildInfo(
        childId: String,
        name: String,
        nickname: String?,
        dob: String?,
        avatarPath: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val updates = buildMap {
                put("name", name)
                put("nickname", nickname)
                put("date_of_birth", dob)
                if (avatarPath != null) {
                    put("avatar_path", avatarPath)
                }
            }
            postgrest.from("children")
                .update(updates) {
                    filter {
                        eq("id", childId)
                    }
                }
            Unit
        }
    }

    override suspend fun createChild(
        familyId: String,
        name: String,
        nickname: String?,
        dob: String?,
        avatarPath: String?
    ): Result<ChildDto> = withContext(Dispatchers.IO) {
        runCatching {
            val request = CreateChildRequestDto(
                familyId = familyId,
                name = name,
                nickname = nickname,
                dateOfBirth = dob,
                avatarPath = avatarPath
            )
            postgrest.from("children")
                .insert(request) {
                    select()
                }
                .decodeSingle<ChildDto>()
        }
    }

    override suspend fun uploadChildAvatar(
        familyId: String,
        childId: String,
        bytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val path = "$familyId/children/$childId/avatar.webp"
            storage.from("family-avatars").upload(path, bytes) {
                upsert = true
            }
            path
        }
    }

    override suspend fun renameDevice(deviceId: String, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("devices")
                .update(
                    mapOf("name" to newName)
                ) {
                    filter {
                        eq("id", deviceId)
                    }
                }
            Unit
        }
    }
}
