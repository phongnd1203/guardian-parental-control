package app.guardian.android.feature.family.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import app.guardian.android.feature.family.data.local.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM family_members WHERE family_id = :familyId ORDER BY CASE role WHEN 'OWNER' THEN 1 WHEN 'PARENT' THEN 2 ELSE 3 END, joined_at ASC")
    fun observeMembers(familyId: String): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE id = :memberId")
    suspend fun getMemberById(memberId: String): FamilyMemberEntity?

    @Upsert
    suspend fun upsertMembers(members: List<FamilyMemberEntity>)

    @Upsert
    suspend fun upsertMember(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :memberId")
    suspend fun deleteMember(memberId: String): Int

    @Query("DELETE FROM family_members WHERE family_id = :familyId")
    suspend fun clearForFamily(familyId: String): Int
}
