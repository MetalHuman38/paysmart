package net.metalbrain.paysmart.core.features.account.creation.viewmodel

import android.app.Activity
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.auth.AuthPolicyHandler
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneVerifier
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.supportedCountries
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val authPolicyHandler: AuthPolicyHandler,
    private val phoneVerifier: PhoneVerifier
) : ViewModel() {

    private val _selectedCountry = mutableStateOf(supportedCountries.first())
    val selectedCountry: State<Country> = _selectedCountry


    fun startPhoneVerification(
        activity: Activity?,
        onSuccess: () -> Unit,
        onPhoneAlreadyRegistered: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (activity == null) {
            onError(IllegalStateException("Activity is required for phone verification."))
            return
        }

        val e164 = getFullPhoneNumber()
        viewModelScope.launch {
            try {
                val exists = authPolicyHandler.isPhoneAlreadyRegistered(e164)
                if (exists) {
                    onPhoneAlreadyRegistered()
                    return@launch
                }

                phoneVerifier.setCallbacks(
                    onCodeSent = {
                        onSuccess()
                    },
                    onError = { error ->
                        // Detect if this came from duplicate phone checks in auth hooks.
                        if (error.message?.contains("already-exists", ignoreCase = true) == true) {
                            onPhoneAlreadyRegistered()
                        } else {
                            onError(error)
                        }
                    }
                )
                phoneVerifier.start(e164, activity)

            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun getFullPhoneNumber(): String {
        return (selectedCountry.value.dialCode + phoneNumber)
            .replace(" ", "")
            .trim()
    }

    var phoneNumber by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var acceptedMarketing by mutableStateOf(false)
        private set


    var gender by mutableStateOf("")
        private set

    var dateOfBirth by mutableStateOf("")
        private set

    var acceptedTerms by mutableStateOf(false)
        private set

    fun onToggleMarketing() {
        acceptedMarketing = !acceptedMarketing
    }

    fun isPhoneValid(): Boolean {
        return phoneNumber.trim().length >= 8
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
