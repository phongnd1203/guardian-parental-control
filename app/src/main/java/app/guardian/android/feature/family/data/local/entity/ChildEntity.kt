package app.guardian.android.feature.family.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.guardian.android.core.model.ChildStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "children",
    indices = [
        Index(value = ["family_id"]),
        Index(value = ["family_id", "status"])
    ]
)
data class ChildEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "family_id")
    val familyId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "nickname")
    val nickname: String? = null,

    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: LocalDate? = null,

    @ColumnInfo(name = "avatar_path")
    val avatarPath: String? = null,

    @ColumnInfo(name = "status")
    val status: ChildStatus = ChildStatus.ACTIVE,

    @ColumnInfo(name = "created_by")
    val createdBy: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,

    @ColumnInfo(name = "archived_at")
    val archivedAt: Instant? = null,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Instant? = null
)
