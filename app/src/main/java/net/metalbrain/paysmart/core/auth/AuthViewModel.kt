package net.metalbrain.paysmart.core.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val config = AuthApiConfig(
        baseUrl = "https://paysmart-api.example.com",
        attachApiPrefix = true
    )

    private val client = AuthPolicyClient(config)

    fun checkBeforeCreate(email: String, displayName: String) {
        viewModelScope.launch {
            when (val result = client.beforeCreate(email, displayName)) {
                is AuthDecision.Allow -> {
                    Log.d("Auth", "Signup allowed")
                }
                is AuthDecision.Deny -> {
                    Log.e("Auth", "Denied: ${result.errorMessage}")
                }
                is AuthDecision.Error -> {
                    Log.e("Auth", "Error: ${result.errorMessage}")
                }
            }
        }
    }
}
