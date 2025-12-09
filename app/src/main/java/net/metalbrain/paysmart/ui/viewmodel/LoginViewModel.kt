package net.metalbrain.paysmart.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.supportedCountries
import net.metalbrain.paysmart.phone.PhoneVerifier

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val phoneVerifier: PhoneVerifier
) : ViewModel() {

    private val _selectedCountry = mutableStateOf(supportedCountries.first())
    val selectedCountry: State<Country> = _selectedCountry

    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isPasswordVisible = mutableStateOf(false)
    val isPasswordVisible: State<Boolean> = _isPasswordVisible

    fun onPhoneNumberChanged(value: String) {
        _phoneNumber.value = value
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
