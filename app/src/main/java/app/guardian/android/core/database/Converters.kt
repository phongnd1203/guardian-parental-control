package app.guardian.android.core.database

import androidx.room.TypeConverter
import app.guardian.android.core.model.ChildStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.InvitationStatus
import app.guardian.android.core.model.ProtectionStatus
import java.time.Instant
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromLocalDateString(value: String?): LocalDate? {
        return value?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromFamilyRole(value: String?): FamilyRole? {
        return value?.let {
            try {
                FamilyRole.valueOf(it)
            } catch (e: Exception) {
                FamilyRole.VIEWER
            }
        }
    }

    @TypeConverter
    fun familyRoleToString(role: FamilyRole?): String? {
        return role?.name
    }

    @TypeConverter
    fun fromChildStatus(value: String?): ChildStatus? {
        return value?.let {
            try {
                ChildStatus.valueOf(it)
            } catch (e: Exception) {
                ChildStatus.ACTIVE
            }
        }
    }

    @TypeConverter
    fun childStatusToString(status: ChildStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun fromProtectionStatus(value: String?): ProtectionStatus? {
        return value?.let {
            try {
                ProtectionStatus.valueOf(it)
            } catch (e: Exception) {
                ProtectionStatus.ACTIVE
            }
        }
    }

    @TypeConverter
    fun protectionStatusToString(status: ProtectionStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun fromInvitationStatus(value: String?): InvitationStatus? {
        return value?.let {
            try {
                InvitationStatus.valueOf(it)
            } catch (e: Exception) {
                InvitationStatus.PENDING
            }
        }
    }

    @TypeConverter
    fun invitationStatusToString(status: InvitationStatus?): String? {
        return status?.name
    }
}
