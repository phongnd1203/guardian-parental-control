package app.guardian.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.data.UserPreferences
import app.guardian.android.data.UserPreferencesRepository
import app.guardian.android.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val initialStartDestination: Screen,
        val preferences: UserPreferences
    ) : MainUiState
}

class MainViewModel(
    application: Application,
    private val repository: UserPreferencesRepository = UserPreferencesRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    init {
        viewModelScope.launch {
            // Read initial preferences from DataStore before NavHost renders
            val initialPrefs = repository.userPreferencesFlow.first()
            val startDestination = Screen.resolveStartDestination(
                isOnboardingCompleted = initialPrefs.isOnboardingCompleted,
                isUserLoggedIn = initialPrefs.isUserLoggedIn
            )
            _uiState.value = MainUiState.Ready(
                initialStartDestination = startDestination,
                preferences = initialPrefs
            )

            // Keep preference state updated
            repository.userPreferencesFlow.collect { prefs ->
                val current = _uiState.value
                if (current is MainUiState.Ready) {
                    _uiState.value = current.copy(preferences = prefs)
                }
            }
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            onSuccess()
        }
    }

    fun signIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.setUserLoggedIn(true)
            onSuccess()
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signOut()
            onSuccess()
        }
    }

    fun resetAll(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.resetAll()
            onSuccess()
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(application) as T
                }
            }
    }
}
