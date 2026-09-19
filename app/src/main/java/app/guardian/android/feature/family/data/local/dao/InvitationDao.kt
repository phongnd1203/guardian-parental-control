package app.guardian.android.feature.family.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import app.guardian.android.feature.family.data.local.entity.InvitationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvitationDao {

    @Query("SELECT * FROM family_invitations WHERE family_id = :familyId AND status = 'PENDING' ORDER BY created_at DESC")
    fun observePendingInvitations(familyId: String): Flow<List<InvitationEntity>>

    @Query("SELECT * FROM family_invitations WHERE id = :invitationId")
    suspend fun getInvitationById(invitationId: String): InvitationEntity?

    @Upsert
    suspend fun upsertInvitations(invitations: List<InvitationEntity>)

    @Upsert
    suspend fun upsertInvitation(invitation: InvitationEntity)

    @Query("DELETE FROM family_invitations WHERE id = :invitationId")
    suspend fun deleteInvitation(invitationId: String): Int

    @Query("DELETE FROM family_invitations WHERE family_id = :familyId")
    suspend fun clearForFamily(familyId: String): Int
}
