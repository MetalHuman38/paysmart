package net.metalbrain.paysmart.core.auth

import org.json.JSONObject

sealed class AuthDecision {
    data class Allow(val httpStatus: Int, val userRecord: JSONObject?) : AuthDecision()
    data class Deny(
        val httpStatus: Int,
        val errorCode: String? = null,
        val errorMessage: String? = null
    ) : AuthDecision()

    data class Error(
        val httpStatus: Int,
        val errorCode: String? = null,
        val errorMessage: String? = null,
        val throwable: Throwable) : AuthDecision()
}
