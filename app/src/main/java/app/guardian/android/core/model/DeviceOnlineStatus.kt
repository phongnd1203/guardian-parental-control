package app.guardian.android.core.model

/**
 * Representation of a child's device connection status.
 */
enum class DeviceOnlineStatus {
    ONLINE,            // Last seen <= 2 minutes
    RECENTLY_ONLINE,   // Last seen between 2 and 15 minutes
    OFFLINE            // Last seen > 15 minutes or never
}
