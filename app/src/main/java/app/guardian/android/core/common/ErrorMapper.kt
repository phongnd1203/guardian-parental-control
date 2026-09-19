package app.guardian.android.core.common

/**
 * Maps backend error codes and exceptions to DomainError and UiText.
 */
object ErrorMapper {

    fun fromCode(code: String?, message: String? = null): DomainError {
        return when (code?.uppercase()) {
            "UNAUTHORIZED" -> DomainError.Unauthorized(message)
            "FORBIDDEN" -> DomainError.Forbidden(message)
            "OWNER_REQUIRED" -> DomainError.OwnerRequired(message)
            "LAST_OWNER_CANNOT_LEAVE" -> DomainError.LastOwnerCannotLeave(message)

            "FAMILY_NOT_FOUND" -> DomainError.FamilyNotFound(message)
            "CHILD_NOT_FOUND" -> DomainError.ChildNotFound(message)
            "DEVICE_NOT_FOUND" -> DomainError.DeviceNotFound(message)
            "MEMBER_NOT_FOUND" -> DomainError.MemberNotFound(message)
            "INVITATION_NOT_FOUND" -> DomainError.InvitationNotFound(message)

            "INVITATION_ALREADY_EXISTS" -> DomainError.InvitationAlreadyExists(message)
            "INVITATION_EXPIRED" -> DomainError.InvitationExpired(message)
            "INVITATION_ALREADY_USED" -> DomainError.InvitationAlreadyUsed(message)
            "MEMBER_ALREADY_EXISTS" -> DomainError.MemberAlreadyExists(message)

            "PAIRING_CODE_INVALID" -> DomainError.PairingCodeInvalid(message)
            "PAIRING_CODE_EXPIRED" -> DomainError.PairingCodeExpired(message)
            "PAIRING_CODE_ALREADY_USED" -> DomainError.PairingCodeAlreadyUsed(message)
            "DEVICE_ALREADY_PAIRED" -> DomainError.DeviceAlreadyPaired(message)

            "NETWORK_UNAVAILABLE" -> DomainError.NetworkUnavailable(message)
            "SERVER_ERROR" -> DomainError.ServerError(message)
            else -> DomainError.Unknown(message)
        }
    }

    fun toUiText(error: DomainError): UiText {
        val message = when (error) {
            is DomainError.Unauthorized -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            is DomainError.Forbidden -> "Bạn không có quyền thực hiện thao tác này."
            is DomainError.OwnerRequired -> "Chỉ có chủ sở hữu gia đình (Owner) mới có quyền này."
            is DomainError.LastOwnerCannotLeave -> "Chủ sở hữu gia đình không thể rời đi hoặc hạ quyền khi chưa chuyển giao quyền quản trị."

            is DomainError.FamilyNotFound -> "Không tìm thấy thông tin gia đình."
            is DomainError.ChildNotFound -> "Không tìm thấy hồ sơ của trẻ."
            is DomainError.DeviceNotFound -> "Không tìm thấy thiết bị."
            is DomainError.MemberNotFound -> "Không tìm thấy thành viên trong gia đình."
            is DomainError.InvitationNotFound -> "Không tìm thấy lời mời hoặc lời mời đã bị hủy."

            is DomainError.InvitationAlreadyExists -> "Lời mời đã được gửi tới email này trước đó."
            is DomainError.InvitationExpired -> "Lời mời đã hết hạn."
            is DomainError.InvitationAlreadyUsed -> "Lời mời này đã được chấp nhận trước đó."
            is DomainError.MemberAlreadyExists -> "Người dùng này đã là thành viên của gia đình."

            is DomainError.PairingCodeInvalid -> "Mã ghép đôi không hợp lệ hoặc không tồn tại."
            is DomainError.PairingCodeExpired -> "Mã ghép đôi đã hết hạn. Vui lòng tạo mã mới trên thiết bị cha mẹ."
            is DomainError.PairingCodeAlreadyUsed -> "Mã ghép đôi này đã được kích hoạt trên thiết bị khác."
            is DomainError.DeviceAlreadyPaired -> "Thiết bị này đã được ghép đôi trước đó."

            is DomainError.NetworkUnavailable -> "Không có kết nối mạng. Vui lòng kiểm tra lại kết nối Internet."
            is DomainError.ServerError -> "Lỗi hệ thống máy chủ. Vui lòng thử lại sau."
            is DomainError.Unknown -> error.originalMessage ?: "Đã xảy ra lỗi không xác định. Vui lòng thử lại."
        }
        return UiText.DynamicString(message)
    }
}
