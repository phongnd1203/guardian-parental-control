package app.guardian.android.core.model

/**
 * Role of a member in a Family.
 */
enum class FamilyRole {
    OWNER,
    PARENT,
    VIEWER;

    val canManageChildren: Boolean
        get() = this == OWNER || this == PARENT

    val canManageDevices: Boolean
        get() = this == OWNER || this == PARENT

    val canManageRules: Boolean
        get() = this == OWNER || this == PARENT

    val canInviteMembers: Boolean
        get() = this == OWNER || this == PARENT

    val canChangeRoles: Boolean
        get() = this == OWNER

    val canRemoveMembers: Boolean
        get() = this == OWNER

    val canDeleteFamily: Boolean
        get() = this == OWNER

    val canTransferOwnership: Boolean
        get() = this == OWNER
}
