package app.guardian.android.feature.family.presentation.child

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.DateTimeUtils
import app.guardian.android.core.common.UiText
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.usecase.CreateChildUseCase
import app.guardian.android.feature.family.domain.usecase.GetChildDetailUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveFamilyUseCase
import app.guardian.android.feature.family.domain.usecase.UpdateChildUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddEditChildUiState(
    val childId: String? = null,
    val isEditMode: Boolean = false,
    val name: String = "",
    val nickname: String = "",
    val dateOfBirth: LocalDate? = null,
    val avatarBytes: ByteArray? = null,
    val currentAvatarPath: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: UiText? = null,
    val isSaved: Boolean = false
) {
    val ageText: String?
        get() = dateOfBirth?.let { DateTimeUtils.formatAgeText(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddEditChildUiState

        if (childId != other.childId) return false
        if (isEditMode != other.isEditMode) return false
        if (name != other.name) return false
        if (nickname != other.nickname) return false
        if (dateOfBirth != other.dateOfBirth) return false
        if (avatarBytes != null) {
            if (other.avatarBytes == null) return false
            if (!avatarBytes.contentEquals(other.avatarBytes)) return false
        } else if (other.avatarBytes != null) return false
        if (currentAvatarPath != other.currentAvatarPath) return false
        if (isLoading != other.isLoading) return false
        if (isSaving != other.isSaving) return false
        if (errorMessage != other.errorMessage) return false
        if (isSaved != other.isSaved) return false

        return true
    }

    override fun hashCode(): Int {
        var result = childId?.hashCode() ?: 0
        result = 31 * result + isEditMode.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + (dateOfBirth?.hashCode() ?: 0)
        result = 31 * result + (avatarBytes?.contentHashCode() ?: 0)
        result = 31 * result + (currentAvatarPath?.hashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isSaving.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + isSaved.hashCode()
        return result
    }
}

class AddEditChildViewModel(
    application: Application,
    private val childId: String?,
    private val observeFamily: ObserveFamilyUseCase,
    private val getChildDetail: GetChildDetailUseCase,
    private val createChild: CreateChildUseCase,
    private val updateChild: UpdateChildUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        AddEditChildUiState(
            childId = childId,
            isEditMode = childId != null,
            isLoading = childId != null
        )
    )
    val uiState: StateFlow<AddEditChildUiState> = _uiState.asStateFlow()

    init {
        if (childId != null) {
            loadChild(childId)
        }
    }

    private fun loadChild(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val child = getChildDetail.getOnce(id)
            if (child != null) {
                _uiState.update {
                    it.copy(
                        name = child.name,
                        nickname = child.nickname.orEmpty(),
                        dateOfBirth = child.dateOfBirth,
                        currentAvatarPath = child.avatarPath,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = UiText.DynamicString("Child profile not found")
                    )
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, errorMessage = null) }
    }

    fun onNicknameChange(newNickname: String) {
        _uiState.update { it.copy(nickname = newNickname) }
    }

    fun onDateOfBirthChange(dob: LocalDate?) {
        if (dob != null && dob.isAfter(LocalDate.now())) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Date of birth cannot be in the future"))
            }
            return
        }
        _uiState.update { it.copy(dateOfBirth = dob, errorMessage = null) }
    }

    fun onAvatarSelected(bytes: ByteArray?) {
        _uiState.update { it.copy(avatarBytes = bytes) }
    }

    fun onSave(onSuccess: (String) -> Unit) {
        val name = _uiState.value.name.trim()
        if (name.isEmpty() || name.length > 50) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Name must be between 1 and 50 characters"))
            }
            return
        }

        val nickname = _uiState.value.nickname.trim().takeIf { it.isNotEmpty() }
        if (nickname != null && nickname.length > 30) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Nickname must not exceed 30 characters"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            if (childId == null) {
                // Add Child Mode
                val family = observeFamily().firstOrNull()
                if (family == null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = UiText.DynamicString("No active family found")
                        )
                    }
                    return@launch
                }

                val result = createChild(
                    familyId = family.id,
                    name = name,
                    nickname = nickname,
                    dob = _uiState.value.dateOfBirth,
                    avatarBytes = _uiState.value.avatarBytes
                )

                result.fold(
                    onSuccess = { created ->
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                        onSuccess(created.id)
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = UiText.DynamicString(err.message ?: "Failed to create child")
                            )
                        }
                    }
                )
            } else {
                // Edit Child Mode
                val result = updateChild(
                    childId = childId,
                    name = name,
                    nickname = nickname,
                    dob = _uiState.value.dateOfBirth,
                    avatarBytes = _uiState.value.avatarBytes
                )

                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                        onSuccess(childId)
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = UiText.DynamicString(err.message ?: "Failed to update child")
                            )
                        }
                    }
                )
            }
        }
    }

    companion object {
        fun provideFactory(application: Application, childId: String?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AddEditChildViewModel(
                        application = application,
                        childId = childId,
                        observeFamily = FamilyDependencyProvider.provideObserveFamilyUseCase(application),
                        getChildDetail = FamilyDependencyProvider.provideGetChildDetailUseCase(application),
                        createChild = FamilyDependencyProvider.provideCreateChildUseCase(application),
                        updateChild = FamilyDependencyProvider.provideUpdateChildUseCase(application)
                    ) as T
                }
            }
    }
}
