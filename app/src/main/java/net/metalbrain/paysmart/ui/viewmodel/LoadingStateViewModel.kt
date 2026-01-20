package net.metalbrain.paysmart.ui.viewmodel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoadingStateViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loadingMessage = MutableStateFlow("Wait...")
    val loadingMessage: StateFlow<String> = _loadingMessage

    // Call this to start loading with rotating messages
    suspend fun startLoading() {
        val messages = listOf(
            "Wait...",
            "Just a moment...",
            "We're on it!",
            "Almost ready...",
        )

        for (message in messages) {
            if (!_isLoading.value) break
            _loadingMessage.emit(message)
            kotlinx.coroutines.delay(2500L)
        }
    }

    fun stopLoading() {
        _isLoading.value = false
    }
}
