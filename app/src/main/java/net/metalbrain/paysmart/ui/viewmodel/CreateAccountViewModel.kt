package net.metalbrain.paysmart.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.phone.PhoneAuthHandler
import net.metalbrain.paysmart.phone.PhoneVerifier

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val phoneVerifier: PhoneVerifier
) : ViewModel() {

    private val _selectedCountry = mutableStateOf(supportedCountries.first())
    val selectedCountry: State<Country> = _selectedCountry

    fun startPhoneVerification(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val e164 = selectedCountry.value.dialCode + phoneNumber
        viewModelScope.launch {
            try {
                if (phoneVerifier is PhoneAuthHandler) {
                    phoneVerifier.setCallbacks(
                        onCodeSent = {
                            onSuccess()
                        },
                        onError = { error ->
                            onError(error)
                        }
                    )
                } else {
                    Log.w("PhoneAuth", "PhoneVerifier is not PhoneAuthHandler. Callbacks not set.")
                }

                phoneVerifier.start(e164, activity)

            } catch (e: Exception) {
                onError(e)
            }
        }
    }


    var phoneNumber by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var acceptedMarketing by mutableStateOf(false)
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var firstName by mutableStateOf("")
        private set

    var lastName by mutableStateOf("")
        private set

    var gender by mutableStateOf("")
        private set

    var dateOfBirth by mutableStateOf("")
        private set

    var acceptedTerms by mutableStateOf(false)
        private set

    fun onEmailChanged(value: String) {
        email = value
    }

    fun onPasswordChanged(value: String) {
        password = value
    }

    fun onToggleMarketing() {
        acceptedMarketing = !acceptedMarketing
    }

    fun isPhoneValid(): Boolean {
        return phoneNumber.trim().length >= 8
    }

    fun isEmailValid(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(): Boolean {
        return password.length >= 6
    }

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
    }

    fun onPhoneNumberChanged(value: String) {
        phoneNumber = value
    }

    fun onToggleTerms() {
        acceptedTerms = !acceptedTerms
    }
}
