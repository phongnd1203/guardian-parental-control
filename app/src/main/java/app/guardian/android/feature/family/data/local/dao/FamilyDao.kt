package app.guardian.android.feature.family.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import app.guardian.android.feature.family.data.local.entity.FamilyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    @Query("SELECT * FROM families WHERE deleted_at IS NULL LIMIT 1")
    fun observeCurrentFamily(): Flow<FamilyEntity?>

    @Query("SELECT * FROM families WHERE id = :familyId AND deleted_at IS NULL")
    fun observeFamily(familyId: String): Flow<FamilyEntity?>

    @Query("SELECT * FROM families WHERE id = :familyId")
    suspend fun getFamilyById(familyId: String): FamilyEntity?

    @Upsert
    suspend fun upsertFamily(family: FamilyEntity)

    @Query("DELETE FROM families WHERE id = :familyId")
    suspend fun deleteFamily(familyId: String): Int

    @Query("DELETE FROM families")
    suspend fun clear(): Int
}
