package app.guardian.android.feature.family.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import app.guardian.android.feature.family.data.local.entity.ChildEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {

    @Query("SELECT * FROM children WHERE family_id = :familyId AND status = 'ACTIVE' AND deleted_at IS NULL ORDER BY name ASC")
    fun observeChildren(familyId: String): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :childId AND deleted_at IS NULL")
    fun observeChildById(childId: String): Flow<ChildEntity?>

    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChildById(childId: String): ChildEntity?

    @Upsert
    suspend fun upsertChildren(children: List<ChildEntity>)

    @Upsert
    suspend fun upsertChild(child: ChildEntity)

    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteChild(childId: String): Int

    @Query("DELETE FROM children WHERE family_id = :familyId")
    suspend fun clearForFamily(familyId: String): Int
}
