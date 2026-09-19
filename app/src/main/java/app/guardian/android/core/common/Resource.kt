package app.guardian.android.core.common

/**
 * Generic class for holding data with its loading status and error state.
 */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val error: DomainError, val message: UiText? = null) : Resource<Nothing>
    data object Loading : Resource<Nothing>

    val dataOrNull: T?
        get() = (this as? Success)?.data
}
