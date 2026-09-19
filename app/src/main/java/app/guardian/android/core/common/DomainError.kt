package app.guardian.android.core.common

/**
 * Standardized domain errors across the application.
 */
sealed class DomainError(val code: String, open val originalMessage: String? = null) {
    // Auth & Permission
    data class Unauthorized(override val originalMessage: String? = null) : DomainError("UNAUTHORIZED", originalMessage)
    data class Forbidden(override val originalMessage: String? = null) : DomainError("FORBIDDEN", originalMessage)
    data class OwnerRequired(override val originalMessage: String? = null) : DomainError("OWNER_REQUIRED", originalMessage)
    data class LastOwnerCannotLeave(override val originalMessage: String? = null) : DomainError("LAST_OWNER_CANNOT_LEAVE", originalMessage)

    // Not found errors
    data class FamilyNotFound(override val originalMessage: String? = null) : DomainError("FAMILY_NOT_FOUND", originalMessage)
    data class ChildNotFound(override val originalMessage: String? = null) : DomainError("CHILD_NOT_FOUND", originalMessage)
    data class DeviceNotFound(override val originalMessage: String? = null) : DomainError("DEVICE_NOT_FOUND", originalMessage)
    data class MemberNotFound(override val originalMessage: String? = null) : DomainError("MEMBER_NOT_FOUND", originalMessage)
    data class InvitationNotFound(override val originalMessage: String? = null) : DomainError("INVITATION_NOT_FOUND", originalMessage)

    // Invitation errors
    data class InvitationAlreadyExists(override val originalMessage: String? = null) : DomainError("INVITATION_ALREADY_EXISTS", originalMessage)
    data class InvitationExpired(override val originalMessage: String? = null) : DomainError("INVITATION_EXPIRED", originalMessage)
    data class InvitationAlreadyUsed(override val originalMessage: String? = null) : DomainError("INVITATION_ALREADY_USED", originalMessage)
    data class MemberAlreadyExists(override val originalMessage: String? = null) : DomainError("MEMBER_ALREADY_EXISTS", originalMessage)

    // Device Pairing errors
    data class PairingCodeInvalid(override val originalMessage: String? = null) : DomainError("PAIRING_CODE_INVALID", originalMessage)
    data class PairingCodeExpired(override val originalMessage: String? = null) : DomainError("PAIRING_CODE_EXPIRED", originalMessage)
    data class PairingCodeAlreadyUsed(override val originalMessage: String? = null) : DomainError("PAIRING_CODE_ALREADY_USED", originalMessage)
    data class DeviceAlreadyPaired(override val originalMessage: String? = null) : DomainError("DEVICE_ALREADY_PAIRED", originalMessage)

    // Network & System
    data class NetworkUnavailable(override val originalMessage: String? = null) : DomainError("NETWORK_UNAVAILABLE", originalMessage)
    data class ServerError(override val originalMessage: String? = null) : DomainError("SERVER_ERROR", originalMessage)
    data class Unknown(override val originalMessage: String? = null) : DomainError("UNKNOWN", originalMessage)
}
