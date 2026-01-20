package net.metalbrain.paysmart.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.PasswordPolicyHandler
import net.metalbrain.paysmart.domain.auth.SocialAuthUseCase
import javax.inject.Inject
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.domain.usecase.EmailLinkUseCase

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val passwordPolicyHandler: PasswordPolicyHandler,
    private val socialAuth: SocialAuthUseCase,
    private val emailLinkUseCase: EmailLinkUseCase,
) : ViewModel() {

    private val _selectedCountry = mutableStateOf(supportedCountries.first())

    val selectedCountry: State<Country> = _selectedCountry

    var phoneNumber by mutableStateOf("")

    val email = mutableStateOf("")

    var loginError by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    fun resetError() {
        loginError = null
    }

    fun handleGoogleSignIn(
        credential: AuthCredential,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            loading = true
            loginError = null
            val result = socialAuth.signInWithGoogle(credential)
            result.onSuccess {
                // Optional password policy check
                try {
                    val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                    if (!idToken.isNullOrBlank()) {
                        val passwordEnabled = passwordPolicyHandler.getPasswordEnabled(idToken)
                        if (!passwordEnabled) {
                            Log.w("LoginViewModel", "Password login not enabled (optional)")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("LoginViewModel", "Failed to fetch claims", e)
                }
                onSuccess()
            }.onFailure {
                Log.e("LoginViewModel", "Google sign-in failed", it)
                onError(it)
            }

            loading = false
        }
    }

    fun linkFederatedCredential(
        credential: AuthCredential,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            loading = true
            val result = socialAuth.linkCredential(credential)
            result.onSuccess { onSuccess() }
                .onFailure { onError(it) }
            loading = false
        }
    }

    fun handleFacebookLogin(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            loading = true
            loginError = null

            val result = socialAuth.signInWithFacebook(activity)
            result.onSuccess {
                onSuccess()
            }.onFailure {
                loginError = "Facebook Sign-In failed: ${it.localizedMessage ?: "Unknown error"}"
                onError(it)
            }

            loading = false
        }
    }




    fun sendMagicLink(
        context: Context,
        email: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
        ) {
        val currentEmail = email.trim()
        if (currentEmail.isBlank()) {
            onError(IllegalArgumentException("Email is required"))
            return
        }
            viewModelScope.launch {
                try {
                    emailLinkUseCase.sendMagicLink(context, email)
                    onSuccess()
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }

    fun handleEmailLoginFromIntent(
            email: String,
            intent: Intent,
            onSuccess: () -> Unit,
            onError: (Throwable) -> Unit
        ) {
            viewModelScope.launch {
                loading = true
                try {
                    val user = emailLinkUseCase.handleEmailLinkIntent(email, intent)
                    Log.d("LoginViewModel", "✅ Magic link sign-in success. UID: ${user.uid}")
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "❌ Magic link sign-in failed", e)
                    loginError = "Magic link sign-in failed"
                    onError(e)
                } finally {
                    loading = false
                }
            }
    }


    fun handleEmailLoginFromIntentError(e: Exception) {
        loginError = "Magic link sign-in failed"
    }



    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isPasswordVisible = mutableStateOf(false)
    val isPasswordVisible: State<Boolean> = _isPasswordVisible

    fun onPhoneNumberChanged(value: String) {
        phoneNumber = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }
}
