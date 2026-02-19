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
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.SecurityRepository
import net.metalbrain.paysmart.domain.auth.SocialAuthUseCase
import javax.inject.Inject
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.domain.usecase.EmailLinkUseCase



enum class GoogleAuthIntent {
    SIGN_IN,
    LINK_PROVIDER
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val security: SecurityRepository,
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
        intent: GoogleAuthIntent,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Log.d("LoginViewModel", "handleGoogleSignIn: intent=$intent")
        viewModelScope.launch {
            loading = true
            loginError = null

            try {
                socialAuth.signInWithGoogle(credential).getOrThrow()
                Log.d(
                    "LoginViewModel",
                    "Google auth successful (intent=$intent, uid=${authRepository.currentUser?.uid})"
                )
                onSuccess()
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Google auth failed (intent=$intent)", e)
                loginError = if (e.message?.contains("Federated login requires verified phone number") == true) {
                    "Please create an account with a phone number before signing in with Google."
                } else {
                    e.localizedMessage
                }
                onError(e)
            } finally {
                loading = false
            }
        }
    }



    fun linkFederatedCredential(
        credential: AuthCredential,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            loading = true
            loginError = null
            try {
                prepareFederatedLinking()
                val result = socialAuth.linkCredential(credential)
                result.onSuccess {
                    Log.d("LoginViewModel", "Successfully linked federated credential.")
                    onSuccess()
                }
                .onFailure {
                    Log.e("LoginViewModel", "Failed to link federated credential.", it)
                    loginError = it.localizedMessage
                    onError(it)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "An exception occurred during federated linking.", e)
                loginError = e.localizedMessage
                onError(e)
            } finally {
                loading = false
            }
        }
    }

    suspend fun prepareFederatedLinking() {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("No authenticated user to link credentials")
        val idToken = user.getIdToken(false).await().token
            ?: throw IllegalStateException("Token missing")

        val result = security.allowFederatedLinking(idToken)
        result.getOrElse { cause ->
            throw IllegalStateException("Unable to authorize federated credential linking", cause)
        }
        Log.d("LoginViewModel", "prepareFederatedLinking: enabled=true")
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
                try {
                    // Just ensure we actually have a valid session
                    val session = authRepository.getCurrentSession()
                        ?: run {
                            Log.w("LoginViewModel", "Facebook sign-in succeeded but no session available yet")
                            onSuccess() // still proceed
                            loading = false
                            return@launch
                        }

                    Log.d(
                        "LoginViewModel",
                        "Facebook sign-in successful for uid=${session.user.uid}"
                    )
                } catch (e: Exception) {
                    Log.w("LoginViewModel", "Session retrieval after FaceBook sign-in failed", e)
                    // Don't block login because of this — still proceed
                }
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

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
    }
}
