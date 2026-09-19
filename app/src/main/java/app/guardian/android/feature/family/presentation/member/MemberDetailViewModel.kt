package app.guardian.android.feature.family.presentation.member

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.UiText
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.network.SupabaseClientProvider
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.repository.FamilyRepository
import app.guardian.android.feature.family.domain.usecase.ObserveFamilyUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveMembersUseCase
import app.guardian.android.feature.family.domain.usecase.RemoveMemberUseCase
import app.guardian.android.feature.family.domain.usecase.UpdateMemberRoleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MemberDetailUiState(
    val member: FamilyMember? = null,
    val isCurrentUserOwner: Boolean = false,
    val isViewingSelf: Boolean = false,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val isRemoving: Boolean = false,
    val errorMessage: UiText? = null,
    val isRemoved: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class MemberDetailViewModel(
    application: Application,
    private val memberId: String,
    private val observeFamily: ObserveFamilyUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val updateMemberRole: UpdateMemberRoleUseCase,
    private val removeMember: RemoveMemberUseCase
) : AndroidViewModel(application) {

    private val currentUserId = SupabaseClientProvider.auth.currentUserOrNull()?.id
    private val _isUpdating = MutableStateFlow(false)
    private val _isRemoving = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<UiText?>(null)
    private val _isRemoved = MutableStateFlow(false)

    val uiState: StateFlow<MemberDetailUiState> = observeFamily()
        .flatMapLatest { family ->
            if (family == null) {
                flowOf(MemberDetailUiState(isLoading = false))
            } else {
                combine(
                    observeMembers(family.id),
                    _isUpdating,
                    _isRemoving,
                    _errorMessage,
                    _isRemoved
                ) { members, isUpdating, isRemoving, errorMessage, isRemoved ->
                    val member = members.firstOrNull { it.id == memberId }
                    val currentMember = members.firstOrNull { it.userId == currentUserId }
                    val isCurrentUserOwner = currentMember?.isOwner == true
                    val isViewingSelf = member?.userId == currentUserId

                    MemberDetailUiState(
                        member = member,
                        isCurrentUserOwner = isCurrentUserOwner,
                        isViewingSelf = isViewingSelf,
                        isLoading = member == null && !isRemoved,
                        isUpdating = isUpdating,
                        isRemoving = isRemoving,
                        errorMessage = errorMessage,
                        isRemoved = isRemoved
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MemberDetailUiState()
        )

    fun onRoleChange(newRole: FamilyRole) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null
            val result = updateMemberRole(memberId, newRole)
            result.fold(
                onSuccess = { _isUpdating.value = false },
                onFailure = { err ->
                    _isUpdating.value = false
                    _errorMessage.value = UiText.DynamicString(err.message ?: "Failed to update member role")
                }
            )
        }
    }

    fun onRemoveMember(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isRemoving.value = true
            _errorMessage.value = null
            val result = removeMember(memberId)
            result.fold(
                onSuccess = {
                    _isRemoving.value = false
                    _isRemoved.value = true
                    onSuccess()
                },
                onFailure = { err ->
                    _isRemoving.value = false
                    _errorMessage.value = UiText.DynamicString(err.message ?: "Failed to remove member")
                }
            )
        }
    }

    companion object {
        fun provideFactory(application: Application, memberId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MemberDetailViewModel(
                        application = application,
                        memberId = memberId,
                        observeFamily = FamilyDependencyProvider.provideObserveFamilyUseCase(application),
                        observeMembers = FamilyDependencyProvider.provideObserveMembersUseCase(application),
                        updateMemberRole = FamilyDependencyProvider.provideUpdateMemberRoleUseCase(application),
                        removeMember = FamilyDependencyProvider.provideRemoveMemberUseCase(application)
                    ) as T
                }
            }
    }
}
