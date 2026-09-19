package app.guardian.android.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.guardian.android.feature.family.data.local.dao.ChildDao
import app.guardian.android.feature.family.data.local.dao.DeviceDao
import app.guardian.android.feature.family.data.local.dao.FamilyDao
import app.guardian.android.feature.family.data.local.dao.InvitationDao
import app.guardian.android.feature.family.data.local.dao.MemberDao
import app.guardian.android.feature.family.data.local.entity.ChildEntity
import app.guardian.android.feature.family.data.local.entity.DeviceEntity
import app.guardian.android.feature.family.data.local.entity.DevicePermissionStatusEntity
import app.guardian.android.feature.family.data.local.entity.FamilyEntity
import app.guardian.android.feature.family.data.local.entity.FamilyMemberEntity
import app.guardian.android.feature.family.data.local.entity.InvitationEntity

@Database(
    entities = [
        FamilyEntity::class,
        ChildEntity::class,
        FamilyMemberEntity::class,
        DeviceEntity::class,
        DevicePermissionStatusEntity::class,
        InvitationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GuardianDatabase : RoomDatabase() {

    abstract fun familyDao(): FamilyDao
    abstract fun childDao(): ChildDao
    abstract fun memberDao(): MemberDao
    abstract fun deviceDao(): DeviceDao
    abstract fun invitationDao(): InvitationDao

    companion object {
        @Volatile
        private var INSTANCE: GuardianDatabase? = null

        fun getInstance(context: Context): GuardianDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GuardianDatabase::class.java,
                    "guardian.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
