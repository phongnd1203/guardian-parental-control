package app.guardian.android.feature.family.presentation.pairing

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.QrCodeGenerator
import app.guardian.android.core.common.UiText
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.PairingSession
import app.guardian.android.feature.family.domain.usecase.CreatePairingSessionUseCase
import app.guardian.android.feature.family.domain.usecase.GetChildDetailUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveDevicesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Duration
import java.time.Instant

data class PairDeviceUiState(
    val child: Child? = null,
    val pairingSession: PairingSession? = null,
    val qrImageBitmap: ImageBitmap? = null,
    val manualCodeFormatted: String = "",
    val remainingSeconds: Long = 0,
    val formattedTime: String = "10:00",
    val isExpired: Boolean = false,
    val isLoading: Boolean = true,
    val isPairingSuccess: Boolean = false,
    val newDeviceName: String? = null,
    val errorMessage: UiText? = null
)

class PairDeviceViewModel(
    application: Application,
    private val childId: String,
    private val getChildDetail: GetChildDetailUseCase,
    private val createPairingSession: CreatePairingSessionUseCase,
    private val observeDevices: ObserveDevicesUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PairDeviceUiState())
    val uiState: StateFlow<PairDeviceUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var observeDevicesJob: Job? = null
    private var initialDeviceIds: Set<String> = emptySet()

    init {
        loadChildAndGenerateSession()
    }

    fun generateNewCode() {
        loadChildAndGenerateSession()
    }

    private fun loadChildAndGenerateSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isExpired = false) }

            val child = getChildDetail.getOnce(childId)
            _uiState.update { it.copy(child = child) }

            // Snapshot initial device IDs to detect when a new device is paired
            if (initialDeviceIds.isEmpty()) {
                val currentDevices = observeDevices(childId).firstOrNull().orEmpty()
                initialDeviceIds = currentDevices.map { it.id }.toSet()
            }

            val sessionResult = createPairingSession(childId)
            sessionResult.fold(
                onSuccess = { session ->
                    val qrPayload = buildJsonObject {
                        put("version", 1)
                        put("pairingToken", session.pairingToken)
                    }.toString()

                    val qrBitmap = QrCodeGenerator.generateImageBitmap(qrPayload, sizePx = 600)

                    val formattedCode = if (session.manualCode.length == 6) {
                        "${session.manualCode.substring(0, 3)} ${session.manualCode.substring(3)}"
                    } else {
                        session.manualCode
                    }

                    _uiState.update {
                        it.copy(
                            pairingSession = session,
                            qrImageBitmap = qrBitmap,
                            manualCodeFormatted = formattedCode,
                            isLoading = false,
                            isExpired = false
                        )
                    }

                    startCountdown(session.expiresAt)
                    startObservingClaimedDevices()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.DynamicString(error.message ?: "Failed to generate pairing code")
                        )
                    }
                }
            )
        }
    }

    private fun startCountdown(expiresAt: Instant) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val now = Instant.now()
                val diff = Duration.between(now, expiresAt).seconds
                if (diff <= 0) {
                    _uiState.update {
                        it.copy(
                            remainingSeconds = 0,
                            formattedTime = "00:00",
                            isExpired = true
                        )
                    }
                    break
                } else {
                    val minutes = diff / 60
                    val seconds = diff % 60
                    val formatted = String.format("%02d:%02d", minutes, seconds)
                    _uiState.update {
                        it.copy(
                            remainingSeconds = diff,
                            formattedTime = formatted,
                            isExpired = false
                        )
                    }
                }
                delay(1000)
            }
        }
    }

    private fun startObservingClaimedDevices() {
        if (observeDevicesJob?.isActive == true) return
        observeDevicesJob = viewModelScope.launch {
            observeDevices(childId).collect { devices ->
                val newDevice = devices.firstOrNull { it.id !in initialDeviceIds }
                if (newDevice != null) {
                    _uiState.update {
                        it.copy(
                            isPairingSuccess = true,
                            newDeviceName = newDevice.name
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        observeDevicesJob?.cancel()
    }

    companion object {
        fun provideFactory(application: Application, childId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PairDeviceViewModel(
                        application = application,
                        childId = childId,
                        getChildDetail = FamilyDependencyProvider.provideGetChildDetailUseCase(application),
                        createPairingSession = FamilyDependencyProvider.provideCreatePairingSessionUseCase(application),
                        observeDevices = FamilyDependencyProvider.provideObserveDevicesUseCase(application)
                    ) as T
                }
            }
    }
}
