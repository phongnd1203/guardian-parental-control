package app.guardian.android.feature.family.data.remote

import app.guardian.android.core.common.DomainError
import app.guardian.android.core.common.ErrorMapper
import app.guardian.android.core.network.SupabaseClientProvider
import app.guardian.android.feature.family.data.remote.dto.ClaimPairingRequest
import app.guardian.android.feature.family.data.remote.dto.ClaimPairingResponse
import app.guardian.android.feature.family.data.remote.dto.CreatePairingRequest
import app.guardian.android.feature.family.data.remote.dto.CreatePairingResponse
import app.guardian.android.feature.family.data.remote.dto.StandardErrorResponse
import app.guardian.android.feature.family.data.remote.dto.UnpairDeviceRequest
import app.guardian.android.feature.family.data.remote.dto.UnpairDeviceResponse
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface FamilyFunctionDataSource {
    suspend fun createFamily(name: String): Result<String>
    suspend fun inviteMember(email: String, role: String): Result<Unit>
    suspend fun acceptInvite(token: String): Result<Unit>
    suspend fun cancelInvite(invitationId: String): Result<Unit>
    suspend fun changeMemberRole(memberId: String, newRole: String): Result<Unit>
    suspend fun removeMember(memberId: String): Result<Unit>
    suspend fun transferOwnership(newOwnerMemberId: String): Result<Unit>
    suspend fun createPairing(childId: String): Result<CreatePairingResponse>
    suspend fun claimPairing(request: ClaimPairingRequest): Result<ClaimPairingResponse>
    suspend fun unpairDevice(deviceId: String): Result<UnpairDeviceResponse>
    suspend fun deleteChild(childId: String): Result<Unit>
    suspend fun deleteFamily(familyId: String): Result<Unit>
}

class SupabaseFamilyFunctionDataSource(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : FamilyFunctionDataSource {

    private val functions = SupabaseClientProvider.functions

    private suspend inline fun <reified T : Any, reified R> invokeFunction(
        functionName: String,
        body: T
    ): Result<R> = withContext(Dispatchers.IO) {
        try {
            val response = functions(functionName, body)
            val text = response.bodyAsText()
            val decoded = json.decodeFromString<R>(text)
            Result.success(decoded)
        } catch (e: RestException) {
            val errorResponse = parseError(e.error)
            val domainError = ErrorMapper.fromCode(errorResponse?.error?.code, errorResponse?.error?.message ?: e.message)
            Result.failure(Exception(domainError.code, e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(errorBody: String?): StandardErrorResponse? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            json.decodeFromString<StandardErrorResponse>(errorBody)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createFamily(name: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject { put("familyName", name) }
            val response = functions("create-family", payload)
            response.bodyAsText()
        }
    }

    override suspend fun inviteMember(email: String, role: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject {
                put("email", email)
                put("role", role)
            }
            functions("invite-family-member", payload)
            Unit
        }
    }

    override suspend fun acceptInvite(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject { put("token", token) }
            functions("accept-family-invite", payload)
            Unit
        }
    }

    override suspend fun cancelInvite(invitationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject { put("invitationId", invitationId) }
            functions("cancel-family-invite", payload)
            Unit
        }
    }

    override suspend fun changeMemberRole(memberId: String, newRole: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject {
                put("memberId", memberId)
                put("newRole", newRole)
            }
            functions("change-member-role", payload)
            Unit
        }
    }

    override suspend fun removeMember(memberId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject { put("memberId", memberId) }
            functions("remove-family-member", payload)
            Unit
        }
    }

    override suspend fun transferOwnership(newOwnerMemberId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject { put("newOwnerMemberId", newOwnerMemberId) }
            functions("transfer-family-ownership", payload)
            Unit
        }
    }

    override suspend fun createPairing(childId: String): Result<CreatePairingResponse> {
        return invokeFunction("create-device-pairing", CreatePairingRequest(childId))
    }

    override suspend fun claimPairing(request: ClaimPairingRequest): Result<ClaimPairingResponse> {
        return invokeFunction("claim-device-pairing", request)
    }

    override suspend fun unpairDevice(deviceId: String): Result<UnpairDeviceResponse> {
        return invokeFunction("unpair-device", UnpairDeviceRequest(deviceId))
    }

    override suspend fun deleteChild(childId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject { put("childId", childId) }
            functions("delete-child", payload)
            Unit
        }
    }

    override suspend fun deleteFamily(familyId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject { put("familyId", familyId) }
            functions("delete-family", payload)
            Unit
        }
    }
}
