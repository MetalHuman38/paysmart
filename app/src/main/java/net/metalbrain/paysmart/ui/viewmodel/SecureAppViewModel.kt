package net.metalbrain.paysmart.ui.viewmodel

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.domain.state.SecureAppScreen
import javax.inject.Inject

@HiltViewModel
class SecureAppViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken.asStateFlow()

    private val _activity = MutableStateFlow<FragmentActivity?>(null)
    val activity: StateFlow<FragmentActivity?> = _activity.asStateFlow()

    private val _uiState = MutableStateFlow<SecureAppScreen>(SecureAppScreen.AppContent)
    val uiState: StateFlow<SecureAppScreen> = _uiState.asStateFlow()

    init {
        fetchIdToken()
    }

    private fun fetchIdToken() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.getIdToken(false)?.await()?.token
            _idToken.value = token
        }
    }
}
