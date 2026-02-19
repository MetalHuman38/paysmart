package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel
class SecurityGateViewModel @Inject constructor(

) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(loading = loading)
    }

}
