package net.metalbrain.paysmart.core.features.account.creation.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.metalbrain.paysmart.core.auth.AuthPolicyHandler
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneVerifier
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.matchCountryByInternationalPrefix
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import net.metalbrain.paysmart.domain.model.supportedCountries
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val authPolicyHandler: AuthPolicyHandler,
    private val phoneVerifier: PhoneVerifier
) : ViewModel() {
    private companion object {
        private const val TAG = "CreateAccountViewModel"
        private const val PHONE_PRECHECK_TIMEOUT_MS = 2_500L
    }

    private val _selectedCountry = mutableStateOf(
        supportedCountries.firstOrNull { it.isoCode == DEFAULT_COUNTRY_ISO2 }
            ?: supportedCountries.first()
    )
    val selectedCountry: State<Country> = _selectedCountry


    fun startPhoneVerification(
        activity: Activity?,
        onVerificationStarted: () -> Unit,
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
                val exists = runCatching {
                    withTimeoutOrNull(PHONE_PRECHECK_TIMEOUT_MS) {
                        authPolicyHandler.isPhoneAlreadyRegistered(e164)
                    } ?: throw IllegalStateException(
                        "Unable to verify phone availability right now. Please retry."
                    )
                }.getOrElse { error ->
                    Log.w(TAG, "Phone availability pre-check failed", error)
                    onError(error)
                    return@launch
                }
                if (exists) {
                    onPhoneAlreadyRegistered()
                    return@launch
                }

                phoneVerifier.cancel()
                phoneVerifier.start(e164, activity)
                onVerificationStarted()

            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun getFullPhoneNumber(): String {
        val nationalDigits = phoneNumber.filter { it.isDigit() }
        val fallback = (selectedCountry.value.dialCode + nationalDigits)
            .replace(" ", "")
            .trim()

        if (nationalDigits.isBlank()) {
            return fallback
        }

        return runCatching {
            val phoneUtil = PhoneNumberUtil.getInstance()
            val parsed = phoneUtil.parse(nationalDigits, selectedCountry.value.isoCode)
            phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        }.getOrElse {
            fallback
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

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
    }

    fun onPhoneNumberChanged(value: String) {
        val matched = matchCountryByInternationalPrefix(value)
        if (matched != null) {
            _selectedCountry.value = matched.first
            phoneNumber = matched.second
            return
        }

        phoneNumber = value.trim().filter { it.isDigit() }.take(15)
    }

    fun onToggleTerms() {
        acceptedTerms = !acceptedTerms
    }
}
