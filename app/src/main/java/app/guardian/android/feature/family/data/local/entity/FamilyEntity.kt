package app.guardian.android.feature.family.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "families",
    indices = [
        Index(value = ["owner_user_id"])
    ]
)
data class FamilyEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "owner_user_id")
    val ownerUserId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant? = null
)
