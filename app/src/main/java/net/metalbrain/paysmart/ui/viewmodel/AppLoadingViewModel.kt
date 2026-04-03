package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.metalbrain.paysmart.ui.screens.loader.LoadingPhase

@HiltViewModel
class AppLoadingViewModel @Inject constructor() : ViewModel() {

    private val _loadingPhase = MutableStateFlow<LoadingPhase>(LoadingPhase.Startup)
    val loadingPhase: StateFlow<LoadingPhase> = _loadingPhase.asStateFlow()

    fun setPhase(phase: LoadingPhase) {
        if (_loadingPhase.value != phase) {
            _loadingPhase.value = phase
        }
    }

    fun complete() {
        _loadingPhase.value = LoadingPhase.Idle
    }
}
