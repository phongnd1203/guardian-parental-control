package app.guardian.android.feature.family.presentation.child

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.UiText
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.Device
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import app.guardian.android.feature.family.domain.usecase.DeleteChildUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveDevicesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildDetailUiState(
    val child: Child? = null,
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val errorMessage: UiText? = null
)

class ChildDetailViewModel(
    application: Application,
    private val childId: String,
    private val repository: FamilyRepository,
    private val observeDevices: ObserveDevicesUseCase,
    private val deleteChildUseCase: DeleteChildUseCase
) : AndroidViewModel(application) {

    private val _isDeleting = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<UiText?>(null)

    val uiState: StateFlow<ChildDetailUiState> = combine(
        repository.observeChild(childId),
        observeDevices(childId),
        _isDeleting,
        _errorMessage
    ) { child, devices, isDeleting, errorMessage ->
        ChildDetailUiState(
            child = child,
            devices = devices,
            isLoading = child == null && isDeleting.not(),
            isDeleting = isDeleting,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChildDetailUiState()
    )

    fun onDeleteChild(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isDeleting.value = true
            _errorMessage.value = null
            val result = deleteChildUseCase(childId)
            result.fold(
                onSuccess = {
                    _isDeleting.value = false
                    onSuccess()
                },
                onFailure = { err ->
                    _isDeleting.value = false
                    _errorMessage.value = UiText.DynamicString(err.message ?: "Failed to delete child profile")
                }
            )
        }
    }

    companion object {
        fun provideFactory(application: Application, childId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChildDetailViewModel(
                        application = application,
                        childId = childId,
                        repository = FamilyDependencyProvider.provideRepository(application),
                        observeDevices = FamilyDependencyProvider.provideObserveDevicesUseCase(application),
                        deleteChildUseCase = FamilyDependencyProvider.provideDeleteChildUseCase(application)
                    ) as T
                }
            }
    }
}
