package net.metalbrain.paysmart.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.FederatedLinkingHandler
import net.metalbrain.paysmart.core.auth.PasswordPolicyHandler
import net.metalbrain.paysmart.data.repository.PasswordRepository
import javax.inject.Inject
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.phone.PhoneDraft
import net.metalbrain.paysmart.phone.PhoneDraftStore

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val phoneDraftStore: PhoneDraftStore,
    private val passwordPolicyHandler: PasswordPolicyHandler,
    private val federatedLinkingHandler: FederatedLinkingHandler,
    private val passwordRepo: PasswordRepository
) : ViewModel() {

    private val _selectedCountry = mutableStateOf(supportedCountries.first())

    private val _phoneDraft = MutableStateFlow(PhoneDraft())

    val selectedCountry: State<Country> = _selectedCountry

    var phoneNumber by mutableStateOf("")

    var loginError by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    fun resetError() {
        loginError = null
    }

    fun attemptLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            loading = true
            loginError = null

            try {
                // ✅ Get cached phone draft once
                val phoneDraft = phoneDraftStore.draft.first()
                Log.d("LoginViewModel", "PhoneDraft: $phoneDraft")

                val cachedPhone = phoneDraft.e164
                Log.d("LoginViewModel", "Cached Phone: $cachedPhone")
                if (cachedPhone.isNullOrBlank()) {
                    loginError = "No cached phone number"
                    return@launch
                }

                // ✅ Verify password
                val passwordOk = passwordRepo.verify(password.value)
                if (!passwordOk) {
                    loginError = "Invalid password"
                    return@launch
                }

                // ✅ Optional online checks
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val idToken = user.getIdToken(false).await().token
                    if (!idToken.isNullOrBlank()) {
                        val passwordEnabled = passwordPolicyHandler.getPasswordEnabled(idToken)
                        if (!passwordEnabled) {
                            loginError = "Password login not enabled for this account"
                            return@launch
                        }
                    }
                }

                // ✅ Success
                onSuccess()

            } catch (e: Exception) {
                loginError = "Login failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                loading = false
            }
        }
    }

    fun handleGoogleSignInSuccess(credential: AuthCredential, onSuccess: () -> Unit) {
        viewModelScope.launch {
            loading = true
            loginError = null

            try {
                val auth = FirebaseAuth.getInstance()

                // Attempt sign-in
                val result = auth.signInWithCredential(credential).await()
                val user = result.user ?: throw Exception("No user returned from sign-in")

                val email = user.email
                val phone = user.phoneNumber

                // 🔍 Check if account already exists (via backend)
                val existingProviders = federatedLinkingHandler.checkFederatedAccountExists(email, phone)

                if (existingProviders.isNotEmpty()) {
                    Log.d("LoginViewModel", "🧠 Account already exists with providers: $existingProviders")
                }

                // 🔐 Check if password login is enabled
                val idToken = user.getIdToken(false).await().token
                if (!idToken.isNullOrBlank()) {
                    val passwordEnabled = passwordPolicyHandler.getPasswordEnabled(idToken)
                    if (!passwordEnabled) {
                        Log.w("LoginViewModel", "Password login is not enabled for this account")
                        // Not necessarily a blocking error
                    }
                }

                Log.d("LoginViewModel", "✅ Google Sign-In successful. UID: ${user.uid}")
                // Continue to home / dashboard
                onSuccess()


            } catch (e: FirebaseAuthUserCollisionException) {
                // 🔁 Credential is already linked to a different account
                Log.e("LoginViewModel", "⚠️ Account linking collision", e)
                loginError = "This account is already in use. Try signing in instead."

            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ Google Sign-In failed", e)
                loginError = "Google Sign-In failed: ${e.localizedMessage ?: "Unknown error"}"

            } finally {
                loading = false
            }
        }
    }


    fun handleGoogleSignInError(e: Exception) {
        loginError = "Google Sign-In failed: ${e.localizedMessage ?: "Unknown error"}"
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
