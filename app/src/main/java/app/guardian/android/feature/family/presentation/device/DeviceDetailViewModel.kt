package app.guardian.android.feature.family.presentation.device

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.UiText
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import app.guardian.android.feature.family.domain.usecase.GetDeviceDetailUseCase
import app.guardian.android.feature.family.domain.usecase.RenameDeviceUseCase
import app.guardian.android.feature.family.domain.usecase.UnpairDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceDetailUiState(
    val device: Device? = null,
    val isLoading: Boolean = true,
    val isRenaming: Boolean = false,
    val isUnpairing: Boolean = false,
    val errorMessage: UiText? = null,
    val isUnpaired: Boolean = false
)

class DeviceDetailViewModel(
    application: Application,
    private val deviceId: String,
    private val repository: FamilyRepository,
    private val renameDeviceUseCase: RenameDeviceUseCase,
    private val unpairDeviceUseCase: UnpairDeviceUseCase
) : AndroidViewModel(application) {

    private val _isRenaming = MutableStateFlow(false)
    private val _isUnpairing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<UiText?>(null)
    private val _isUnpaired = MutableStateFlow(false)

    val uiState: StateFlow<DeviceDetailUiState> = combine(
        repository.observeDevice(deviceId),
        _isRenaming,
        _isUnpairing,
        _errorMessage,
        _isUnpaired
    ) { device, isRenaming, isUnpairing, errorMessage, isUnpaired ->
        DeviceDetailUiState(
            device = device,
            isLoading = device == null && !isUnpaired,
            isRenaming = isRenaming,
            isUnpairing = isUnpairing,
            errorMessage = errorMessage,
            isUnpaired = isUnpaired
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DeviceDetailUiState()
    )

    fun onRename(newName: String, onComplete: () -> Unit) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _isRenaming.value = true
            _errorMessage.value = null
            val result = renameDeviceUseCase(deviceId, trimmed)
            result.fold(
                onSuccess = {
                    _isRenaming.value = false
                    onComplete()
                },
                onFailure = { err ->
                    _isRenaming.value = false
                    _errorMessage.value = UiText.DynamicString(err.message ?: "Failed to rename device")
                }
            )
        }
    }

    fun onUnpair(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUnpairing.value = true
            _errorMessage.value = null
            val result = unpairDeviceUseCase(deviceId)
            result.fold(
                onSuccess = {
                    _isUnpairing.value = false
                    _isUnpaired.value = true
                    onSuccess()
                },
                onFailure = { err ->
                    _isUnpairing.value = false
                    _errorMessage.value = UiText.DynamicString(err.message ?: "Failed to unpair device")
                }
            )
        }
    }

    companion object {
        fun provideFactory(application: Application, deviceId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeviceDetailViewModel(
                        application = application,
                        deviceId = deviceId,
                        repository = FamilyDependencyProvider.provideRepository(application),
                        renameDeviceUseCase = FamilyDependencyProvider.provideRenameDeviceUseCase(application),
                        unpairDeviceUseCase = FamilyDependencyProvider.provideUnpairDeviceUseCase(application)
                    ) as T
                }
            }
    }
}
