package net.metalbrain.paysmart.core.features.fx.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.fx.data.FxFeeLine
import net.metalbrain.paysmart.core.features.fx.data.FxQuote
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteQuery
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteResult
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.room.doa.FxQuoteCacheDao
import net.metalbrain.paysmart.room.entity.FxQuoteCacheEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FxQuoteRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val cacheDao: FxQuoteCacheDao
) {

    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getQuote(query: FxQuoteQuery): Result<FxQuoteResult> = runCatching {
        val normalized = query.normalize()
        require(normalized.sourceCurrency.length == 3) { "Invalid source currency" }
        require(normalized.targetCurrency.length == 3) { "Invalid target currency" }
        require(normalized.sourceAmount > 0.0) { "Invalid source amount" }

        val uid = authRepository.currentUser?.uid ?: "anonymous"
        val cacheKey = buildCacheKey(uid, normalized)

        runCatching {
            fetchServerQuote(normalized)
        }.onSuccess { serverQuote ->
            cacheDao.upsert(
                FxQuoteCacheEntity(
                    cacheKey = cacheKey,
                    userId = uid,
                    sourceCurrency = serverQuote.sourceCurrency,
                    targetCurrency = serverQuote.targetCurrency,
                    sourceAmount = serverQuote.sourceAmount,
                    method = normalized.method.apiCode,
                    rate = serverQuote.rate,
                    recipientAmount = serverQuote.recipientAmount,
                    feesJson = feesToJson(serverQuote.fees),
                    guaranteeSeconds = serverQuote.guaranteeSeconds,
                    arrivalSeconds = serverQuote.arrivalSeconds,
                    rateSource = serverQuote.rateSource,
                    updatedAtMs = serverQuote.updatedAtMs
                )
            )
            return@runCatching FxQuoteResult(
                quote = serverQuote,
                dataSource = FxQuoteDataSource.SERVER
            )
        }

        val cached = cacheDao.getByCacheKey(cacheKey)
            ?: throw IllegalStateException("Live quote unavailable and no cached quote found")
        FxQuoteResult(
            quote = cached.toDomain(),
            dataSource = FxQuoteDataSource.CACHE
        )
    }

    fun observeCachedQuote(query: FxQuoteQuery): Flow<FxQuote?> {
        val uid = authRepository.currentUser?.uid ?: "anonymous"
        val cacheKey = buildCacheKey(uid, query.normalize())
        return cacheDao.observeByCacheKey(cacheKey).map { it?.toDomain() }
    }

    private suspend fun fetchServerQuote(query: FxQuoteQuery): FxQuote {
        return withContext(Dispatchers.IO) {
            val url = buildQuotesUrl(query)
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException(parseErrorMessage(body))
                }
                if (body.isBlank()) {
                    throw IllegalStateException("Quote API returned empty body")
                }

                val json = JSONObject(body)
                val fees = json.optJSONArray("fees").toFeeLines()
                val rateSource = response.header("X-Rate-Source")
                    ?.trim()
                    ?.ifBlank { null }
                    ?: "upstream"

                FxQuote(
                    sourceCurrency = json.getString("sourceCurrency").uppercase(Locale.US),
                    targetCurrency = json.getString("targetCurrency").uppercase(Locale.US),
                    sourceAmount = json.getDouble("sourceAmount"),
                    rate = json.getDouble("rate"),
                    recipientAmount = json.getDouble("recipientAmount"),
                    fees = fees,
                    guaranteeSeconds = json.optInt("guaranteeSeconds", 0),
                    arrivalSeconds = json.optInt("arrivalSeconds", 0),
                    rateSource = rateSource,
                    updatedAtMs = System.currentTimeMillis()
                )
            }
        }
    }

    private fun buildQuotesUrl(query: FxQuoteQuery): String {
        val base = config.apiBase.trimEnd('/')
        return "$base/quotes" +
            "?source=${query.sourceCurrency}" +
            "&target=${query.targetCurrency}" +
            "&amount=${query.sourceAmount}" +
            "&method=${query.method.apiCode}"
    }

    private fun buildCacheKey(uid: String, query: FxQuoteQuery): String {
        val roundedAmount = String.format(Locale.US, "%.2f", query.sourceAmount)
        return listOf(
            uid.trim(),
            query.sourceCurrency,
            query.targetCurrency,
            roundedAmount,
            query.method.apiCode
        ).joinToString("|")
    }

    private fun parseErrorMessage(rawBody: String): String {
        val fallback = "Unable to fetch live quote"
        if (rawBody.isBlank()) return fallback
        return runCatching {
            JSONObject(rawBody).optString("error", fallback).ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

private fun FxQuoteQuery.normalize(): FxQuoteQuery {
    return copy(
        sourceCurrency = sourceCurrency.trim().uppercase(Locale.US),
        targetCurrency = targetCurrency.trim().uppercase(Locale.US)
    )
}

private fun JSONArray?.toFeeLines(): List<FxFeeLine> {
    if (this == null) return emptyList()
    val result = mutableListOf<FxFeeLine>()
    for (index in 0 until length()) {
        val item = optJSONObject(index) ?: continue
        result += FxFeeLine(
            label = item.optString("label"),
            amount = item.optDouble("amount", 0.0),
            code = item.optString("code").ifBlank { null }
        )
    }
    return result
}

private fun feesToJson(fees: List<FxFeeLine>): String {
    val array = JSONArray()
    fees.forEach { fee ->
        array.put(
            JSONObject()
                .put("label", fee.label)
                .put("amount", fee.amount)
                .put("code", fee.code)
        )
    }
    return array.toString()
}

private fun FxQuoteCacheEntity.toDomain(): FxQuote {
    val fees = runCatching { JSONArray(feesJson).toFeeLines() }
        .getOrDefault(emptyList())

    return FxQuote(
        sourceCurrency = sourceCurrency,
        targetCurrency = targetCurrency,
        sourceAmount = sourceAmount,
        rate = rate,
        recipientAmount = recipientAmount,
        fees = fees,
        guaranteeSeconds = guaranteeSeconds,
        arrivalSeconds = arrivalSeconds,
        rateSource = rateSource,
        updatedAtMs = updatedAtMs
    )
}
