package net.metalbrain.paysmart.core.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class AddressLookupPayload(
    val house: String,
    val postcode: String,
    val country: String,
    val lat: Double? = null,
    val lng: Double? = null
)

data class AddressLookupResult(
    val fullAddress: String,
    val fullAddressWithHouse: String,
    val postCode: String,
    val countryCode: String,
    val houseInfo: String,
    val lat: Double,
    val lng: Double,
    val line1: String,
    val line2: String?,
    val city: String?,
    val stateOrRegion: String?,
    val source: String?
)

class AddressResolverPolicyClient(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
) {
    companion object {
        private val defaultClient = OkHttpClient.Builder()
            .callTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    suspend fun lookupAddress(
        idToken: String,
        payload: AddressLookupPayload
    ): Result<AddressLookupResult> = withContext(Dispatchers.IO) {
        runCatching {
            val requestBody = JSONObject()
                .put("house", payload.house.trim())
                .put("postcode", payload.postcode.trim())
                .put("country", payload.country.trim().lowercase())
                .apply {
                    payload.lat?.let { put("lat", it) }
                    payload.lng?.let { put("lng", it) }
                }
                .toString()
                .toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(config.lookupAddressUrl)
                .header("Authorization", "Bearer $idToken")
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = extractError(rawBody)
                    throw IllegalStateException(message)
                }

                if (rawBody.isBlank()) {
                    throw IllegalStateException("Address resolver returned an empty response")
                }

                parseAddress(rawBody)
            }
        }
    }

    private fun parseAddress(rawBody: String): AddressLookupResult {
        val json = JSONObject(rawBody)
        return AddressLookupResult(
            fullAddress = json.optString("fullAddress"),
            fullAddressWithHouse = json.optString("fullAddressWithHouse"),
            postCode = json.optString("postCode"),
            countryCode = json.optString("countryCode"),
            houseInfo = json.optString("houseInfo"),
            lat = json.optDouble("lat"),
            lng = json.optDouble("lng"),
            line1 = json.optString("line1"),
            line2 = json.optNullableString("line2"),
            city = json.optNullableString("city"),
            stateOrRegion = json.optNullableString("stateOrRegion"),
            source = json.optNullableString("source")
        )
    }

    private fun extractError(rawBody: String): String {
        if (rawBody.isBlank()) {
            return "Unable to resolve address"
        }
        return runCatching {
            JSONObject(rawBody).optString("error")
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Unable to resolve address"
    }
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key)) return null
    return optString(key).trim().takeIf { it.isNotEmpty() }
}
