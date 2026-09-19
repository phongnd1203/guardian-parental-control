package app.guardian.android.feature.family.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.guardian.android.core.model.FamilyRole
import java.time.Instant

@Entity(
    tableName = "family_members",
    indices = [
        Index(value = ["family_id"]),
        Index(value = ["user_id"]),
        Index(value = ["family_id", "user_id"], unique = true),
        Index(value = ["family_id", "role"])
    ]
)
data class FamilyMemberEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "family_id")
    val familyId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "role")
    val role: FamilyRole = FamilyRole.VIEWER,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "joined_at")
    val joinedAt: Instant? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant? = null
)
