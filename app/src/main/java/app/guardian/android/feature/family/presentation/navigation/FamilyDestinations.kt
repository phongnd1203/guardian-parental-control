package app.guardian.android.feature.family.presentation.navigation

/**
 * Route definitions and parameter helpers for Family module navigation.
 */
object FamilyDestinations {
    const val HOME = "family"
    const val CREATE_FAMILY = "family/create"
    const val ADD_CHILD = "family/child/add"

    const val CHILD_DETAIL = "family/child/{childId}"
    const val EDIT_CHILD = "family/child/{childId}/edit"
    const val PAIR_DEVICE = "family/child/{childId}/pair-device"

    const val DEVICE_DETAIL = "family/device/{deviceId}"
    const val DEVICE_PERMISSIONS = "family/device/{deviceId}/permissions"

    const val MEMBER_DETAIL = "family/member/{memberId}"
    const val INVITE_PARENT = "family/invite"
    const val FAMILY_SETTINGS = "family/settings"

    const val ACCEPT_INVITE = "family/invite-accept/{token}"
    const val INVITE_DEEP_LINK_URI_PATTERN = "https://guardian.example.com/invite/{token}"

    fun childDetail(childId: String): String = "family/child/$childId"
    fun editChild(childId: String): String = "family/child/$childId/edit"
    fun pairDevice(childId: String): String = "family/child/$childId/pair-device"

    fun deviceDetail(deviceId: String): String = "family/device/$deviceId"
    fun devicePermissions(deviceId: String): String = "family/device/$deviceId/permissions"

    fun memberDetail(memberId: String): String = "family/member/$memberId"
    fun acceptInvite(token: String): String = "family/invite-accept/$token"
}
