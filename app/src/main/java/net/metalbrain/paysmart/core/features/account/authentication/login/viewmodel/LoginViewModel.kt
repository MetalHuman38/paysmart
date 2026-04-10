package net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel

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
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.core.auth.providers.GoogleAuthIntent
import net.metalbrain.paysmart.core.features.account.passkey.repository.PasskeyApiRepository
import net.metalbrain.paysmart.core.features.account.passkey.repository.PasskeyCredentialManager
import net.metalbrain.paysmart.core.features.account.security.repository.SecurityRepository
import net.metalbrain.paysmart.core.features.account.security.mfa.provider.MfaSignInProvider
import net.metalbrain.paysmart.domain.auth.SocialAuthUseCase
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailDraft
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailDraftStore
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.matchCountryByInternationalPrefix
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.domain.usecase.EmailLinkUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val passkeyApiRepository: PasskeyApiRepository,
    private val passkeyCredentialManager: PasskeyCredentialManager,
    private val security: SecurityRepository,
    private val mfaSignInProvider: MfaSignInProvider,
    private val socialAuth: SocialAuthUseCase,
    private val emailLinkUseCase: EmailLinkUseCase,
    private val emailDraftStore: EmailDraftStore,
) : ViewModel() {

    private val _selectedCountry = mutableStateOf(
        supportedCountries.firstOrNull { it.isoCode == DEFAULT_COUNTRY_ISO2 }
            ?: supportedCountries.first()
    )

    val selectedCountry: State<Country> = _selectedCountry

    var phoneNumber by mutableStateOf("")

    val email = mutableStateOf("")

    var loginError by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var passkeyLoading by mutableStateOf(false)
        private set

    private var autoPasskeyAttempted = false

    private var handledEmailLink: String? = null

    fun handleGoogleSignIn(
        credential: AuthCredential,
        intent: GoogleAuthIntent,
        onSuccess: () -> Unit,
        onMfaRequired: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Log.d("LoginViewModel", "handleGoogleSignIn: intent=$intent")
        viewModelScope.launch {
            loading = true
            loginError = null

            try {
                socialAuth.signInWithGoogle(credential).getOrThrow()
                Log.d("LoginViewModel", "Google auth successful (intent=$intent)")
                onSuccess()
            } catch (e: Exception) {
                if (handleMfaChallenge(e, onMfaRequired, onError)) {
                    return@launch
                }
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

    fun signInWithPasskey(
        activity: Activity,
        autoAttempt: Boolean,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        if (passkeyLoading || loading) return
        if (autoAttempt && autoPasskeyAttempted) return
        if (autoAttempt) {
            autoPasskeyAttempted = true
        }

        viewModelScope.launch {
            passkeyLoading = true
            if (!autoAttempt) {
                loading = true
                loginError = null
            }
            try {
                val options = passkeyApiRepository.fetchSignInOptions().getOrThrow()
                val assertionJson = passkeyCredentialManager.getAssertion(
                    activity = activity,
                    requestJson = options,
                    preferImmediatelyAvailableCredentials = autoAttempt
                ).getOrThrow()
                val verification = passkeyApiRepository.verifySignIn(assertionJson).getOrThrow()
                if (!verification.verified || verification.customToken.isBlank()) {
                    throw IllegalStateException("Passkey sign-in verification failed")
                }
                authRepository.signInWithCustomToken(verification.customToken)
                Log.d("LoginViewModel", "Passkey sign-in successful uid=${verification.uid}")
                onSuccess()
            } catch (error: Exception) {
                if (autoAttempt) {
                    Log.d(
                        "LoginViewModel",
                        "Auto passkey sign-in skipped: ${error.localizedMessage ?: "unknown error"}"
                    )
                } else {
                    loginError = error.localizedMessage ?: "Unable to sign in with passkey"
                    onError(error)
                }
            } finally {
                passkeyLoading = false
                if (!autoAttempt) {
                    loading = false
                }
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
        onMfaRequired: () -> Unit,
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
                        "Facebook sign-in successful"
                    )
                } catch (e: Exception) {
                    Log.w("LoginViewModel", "Session retrieval after FaceBook sign-in failed", e)
                    // Don't block login because of this — still proceed
                }
                onSuccess()
            }.onFailure {
                if (handleMfaChallenge(it, onMfaRequired, onError)) {
                    loading = false
                    return@launch
                }
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
                emailLinkUseCase.sendMagicLink(context, currentEmail)
                emailDraftStore.saveDraft(
                    EmailDraft(email = currentEmail, verified = false)
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun handleEmailLoginFromIntent(
        intent: Intent,
        onSuccess: () -> Unit,
        onMfaRequired: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val emailLink = intent.data?.toString()?.trim().orEmpty()
        if (emailLink.isBlank()) return
        if (!FirebaseAuth.getInstance().isSignInWithEmailLink(emailLink)) return
        if (emailLink == handledEmailLink) return

        handledEmailLink = emailLink
        viewModelScope.launch {
            loading = true
            try {
                val storedEmail = emailDraftStore.draft.first().email?.trim().orEmpty()
                val resolvedEmail = storedEmail.ifBlank { email.value.trim() }
                if (resolvedEmail.isBlank()) {
                    throw IllegalStateException("Enter your email to complete sign-in")
                }

                val user = emailLinkUseCase.handleEmailLinkIntent(resolvedEmail, intent)
                emailDraftStore.saveDraft(
                    EmailDraft(email = user.email ?: resolvedEmail, verified = true)
                )
                Log.d("LoginViewModel", "Magic link sign-in success")
                onSuccess()
            } catch (e: Exception) {
                if (handleMfaChallenge(e, onMfaRequired, onError)) {
                    loading = false
                    return@launch
                }
                handledEmailLink = null
                Log.e("LoginViewModel", "Magic link sign-in failed", e)
                loginError = "Magic link sign-in failed"
                onError(e)
            } finally {
                loading = false
            }
        }
    }

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    fun onPhoneNumberChanged(value: String) {
        val matched = matchCountryByInternationalPrefix(value)
        if (matched != null) {
            _selectedCountry.value = matched.first
            phoneNumber = matched.second
            return
        }
        phoneNumber = value.trim().filter { it.isDigit() }.take(15)
    }

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
    }

    fun autoSelectCountry(rawIso2: String?) {
        val normalizedIso2 = normalizeCountryIso2(rawIso2, DEFAULT_COUNTRY_ISO2)
        val match = supportedCountries.firstOrNull {
            it.isoCode.equals(normalizedIso2, ignoreCase = true)
        } ?: return

        val shouldReplaceSelection =
            phoneNumber.isBlank() ||
                selectedCountry.value.isoCode == DEFAULT_COUNTRY_ISO2
        if (shouldReplaceSelection) {
            _selectedCountry.value = match
        }
    }

    private fun handleMfaChallenge(
        error: Throwable,
        onMfaRequired: () -> Unit,
        onError: (Throwable) -> Unit
    ): Boolean {
        val mfaException = error as? FirebaseAuthMultiFactorException ?: return false
        val startResult = mfaSignInProvider.beginChallenge(mfaException)
        return if (startResult.isSuccess) {
            loginError = null
            onMfaRequired()
            true
        } else {
            val cause = startResult.exceptionOrNull() ?: error
            loginError = cause.localizedMessage
            onError(cause)
            false
        }
    }
}
