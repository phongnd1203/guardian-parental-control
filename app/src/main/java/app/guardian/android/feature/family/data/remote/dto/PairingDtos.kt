package app.guardian.android.feature.family.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePairingRequest(
    @SerialName("childId")
    val childId: String
)

@Serializable
data class CreatePairingResponse(
    @SerialName("pairingSessionId")
    val pairingSessionId: String,
    @SerialName("pairingToken")
    val pairingToken: String,
    @SerialName("manualCode")
    val manualCode: String,
    @SerialName("expiresAt")
    val expiresAt: String
)

@Serializable
data class ClaimPairingRequest(
    @SerialName("pairingToken")
    val pairingToken: String? = null,
    @SerialName("manualCode")
    val manualCode: String? = null,
    @SerialName("device")
    val device: DeviceInfoPayload
)

@Serializable
data class DeviceInfoPayload(
    @SerialName("name")
    val name: String,
    @SerialName("manufacturer")
    val manufacturer: String? = null,
    @SerialName("model")
    val model: String? = null,
    @SerialName("androidVersion")
    val androidVersion: String? = null,
    @SerialName("apiLevel")
    val apiLevel: Int? = null,
    @SerialName("appVersion")
    val appVersion: String? = null,
    @SerialName("appBuild")
    val appBuild: Int? = null
)

@Serializable
data class ClaimPairingResponse(
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("childId")
    val childId: String,
    @SerialName("familyId")
    val familyId: String,
    @SerialName("pairedAt")
    val pairedAt: String
)

@Serializable
data class UnpairDeviceRequest(
    @SerialName("deviceId")
    val deviceId: String
)

@Serializable
data class UnpairDeviceResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("unpairedAt")
    val unpairedAt: String? = null
)
