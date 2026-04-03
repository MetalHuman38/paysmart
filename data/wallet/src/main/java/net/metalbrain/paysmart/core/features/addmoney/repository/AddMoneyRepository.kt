package net.metalbrain.paysmart.core.features.addmoney.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
import net.metalbrain.paysmart.core.service.performance.AppPerformanceMonitor
import net.metalbrain.paysmart.data.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddMoneyRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val performanceMonitor: AppPerformanceMonitor
) {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun createSession(
        amountMinor: Int,
        currency: String,
        provider: AddMoneyProvider
    ): Result<AddMoneySessionData> = runCatching {
        performanceMonitor.trace(
            name = "add_money_create_session",
            attributes = mapOf(
                "currency" to currency.uppercase(),
                "provider" to provider.name.lowercase()
            )
        ) {
            val session = authRepository.getCurrentSessionOrThrow()
            val requestBody = JSONObject()
                .put("amountMinor", amountMinor)
                .put("currency", currency.uppercase())
                .toString()
                .toRequestBody("application/json".toMediaTypeOrNull())

            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(createSessionUrl(provider))
                    .header("Authorization", "Bearer ${session.idToken}")
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw parseAddMoneyApiException(
                            statusCode = response.code,
                            rawBody = rawBody,
                            fallbackMessage = "Unable to create add money session"
                        )
                    }

                    parseAddMoneySession(rawBody, provider)
                }
            }
        }
    }

    suspend fun getSessionStatus(
        sessionId: String,
        provider: AddMoneyProvider
    ): Result<AddMoneySessionData> = runCatching {
        performanceMonitor.trace(
            name = "add_money_get_session_status",
            attributes = mapOf(
                "session_id" to sessionId.take(40),
                "provider" to provider.name.lowercase()
            )
        ) {
            val session = authRepository.getCurrentSessionOrThrow()
            val request = Request.Builder()
                .url(sessionStatusUrl(provider, sessionId))
                .header("Authorization", "Bearer ${session.idToken}")
                .get()
                .build()

            withContext(Dispatchers.IO) {
                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw parseAddMoneyApiException(
                            statusCode = response.code,
                            rawBody = rawBody,
                            fallbackMessage = "Unable to load add money status"
                        )
                    }

                    parseAddMoneySession(rawBody, provider)
                }
            }
        }
    }

    private fun createSessionUrl(provider: AddMoneyProvider): String {
        return when (provider) {
            AddMoneyProvider.STRIPE -> config.addMoneySessionUrl
            AddMoneyProvider.FLUTTERWAVE -> config.addMoneyFlutterwaveSessionUrl
        }
    }

    private fun sessionStatusUrl(provider: AddMoneyProvider, sessionId: String): String {
        return when (provider) {
            AddMoneyProvider.STRIPE -> config.addMoneySessionStatusUrl(sessionId)
            AddMoneyProvider.FLUTTERWAVE -> config.addMoneyFlutterwaveSessionStatusUrl(sessionId)
        }
    }
}
