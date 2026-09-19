package app.guardian.android.feature.family.domain.repository

import app.guardian.android.core.model.FamilyRole
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.model.Family
import app.guardian.android.feature.family.domain.model.FamilyInvitation
import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.model.PairingSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FamilyRepository {
    fun observeFamily(): Flow<Family?>
    fun observeChildren(familyId: String): Flow<List<Child>>
    fun observeMembers(familyId: String): Flow<List<FamilyMember>>
    fun observeDevices(childId: String): Flow<List<Device>>
    fun observeInvitations(familyId: String): Flow<List<FamilyInvitation>>

    suspend fun refreshFamily()
    suspend fun createFamily(name: String): Result<String>
    suspend fun updateChild(childId: String, name: String, nickname: String?, dob: LocalDate?): Result<Unit>
    suspend fun deleteChild(childId: String): Result<Unit>
    suspend fun createPairingSession(childId: String): Result<PairingSession>
    suspend fun unpairDevice(deviceId: String): Result<Unit>
    suspend fun renameDevice(deviceId: String, newName: String): Result<Unit>
    suspend fun inviteMember(email: String, role: FamilyRole): Result<Unit>
    suspend fun cancelInvitation(invitationId: String): Result<Unit>
    suspend fun acceptInvitation(token: String): Result<Unit>
    suspend fun updateMemberRole(memberId: String, newRole: FamilyRole): Result<Unit>
    suspend fun removeMember(memberId: String): Result<Unit>
}
