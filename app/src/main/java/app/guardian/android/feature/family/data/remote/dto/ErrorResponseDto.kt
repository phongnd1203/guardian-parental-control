package app.guardian.android.feature.family.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandardErrorResponse(
    @SerialName("error")
    val error: ErrorDetailDto
)

@Serializable
data class ErrorDetailDto(
    @SerialName("code")
    val code: String,
    @SerialName("message")
    val message: String,
    @SerialName("requestId")
    val requestId: String? = null
)
