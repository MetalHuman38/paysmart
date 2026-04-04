package net.metalbrain.paysmart.core.features.fundingaccount.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountErrorCode
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountKyc
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountProvisionResult
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountStatus
import net.metalbrain.paysmart.core.service.performance.AppPerformanceMonitor
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.room.dao.FundingAccountDao
import net.metalbrain.paysmart.room.entity.FundingAccountEntity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundingAccountRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val dao: FundingAccountDao,
    private val performanceMonitor: AppPerformanceMonitor
) : FundingAccountGateway {
    private val config = AuthApiConfig(
        baseUrl = Env.apiBase,
        attachApiPrefix = true
    )

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrent(): Flow<FundingAccountData?> {
        return authRepository.authChanges.flatMapLatest { isLoggedIn ->
            val userId = authRepository.currentUser?.uid?.trim().orEmpty()
            if (!isLoggedIn || userId.isEmpty()) {
                flowOf(null)
            } else {
                dao.observeByUserId(userId).map { entity -> entity?.toDomain() }
            }
        }
    }

    override suspend fun syncFromServer(): Result<FundingAccountData?> = runCatching {
        performanceMonitor.trace(name = "funding_account_sync") {
            val session = authRepository.getCurrentSessionOrThrow()
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(config.flutterwaveFundingAccountUrl)
                    .header("Authorization", "Bearer ${session.idToken}")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val error = parseFundingAccountApiException(
                            statusCode = response.code,
                            rawBody = rawBody,
                            fallbackMessage = "Unable to load funding account"
                        )
                        if (error.statusCode == 404 &&
                            error.code == FundingAccountErrorCode.FLUTTERWAVE_FUNDING_ACCOUNT_NOT_FOUND
                        ) {
                            dao.deleteByUserId(session.user.uid)
                            return@use null
                        }
                        throw error
                    }

                    val account = parseFundingAccount(rawBody)
                    dao.upsert(account.toEntity(session.user.uid))
                    account
                }
            }
        }
    }

    override suspend fun provision(
        kyc: FundingAccountKyc?
    ): Result<FundingAccountProvisionResult> = runCatching {
        performanceMonitor.trace(name = "funding_account_provision") {
            val session = authRepository.getCurrentSessionOrThrow()
            val requestBody = buildProvisionRequestBody(kyc)

            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(config.flutterwaveFundingAccountProvisionUrl)
                    .header("Authorization", "Bearer ${session.idToken}")
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw parseFundingAccountApiException(
                            statusCode = response.code,
                            rawBody = rawBody,
                            fallbackMessage = "Unable to provision funding account"
                        )
                    }

                    val result = parseFundingAccountProvisionResult(rawBody)
                    dao.upsert(result.account.toEntity(session.user.uid))
                    result
                }
            }
        }
    }

    private fun buildProvisionRequestBody(kyc: FundingAccountKyc?): okhttp3.RequestBody {
        val json = JSONObject()
        val bvn = sanitizeIdentifier(kyc?.bvn)
        val nin = sanitizeIdentifier(kyc?.nin)
        if (bvn != null || nin != null) {
            json.put(
                "kyc",
                JSONObject().apply {
                    if (bvn != null) put("bvn", bvn)
                    if (nin != null) put("nin", nin)
                }
            )
        }
        return json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }
}

private fun FundingAccountEntity.toDomain(): FundingAccountData {
    return FundingAccountData(
        accountId = accountId,
        provider = provider,
        currency = currency,
        accountNumber = accountNumber,
        bankName = bankName,
        accountName = accountName,
        reference = reference,
        status = FundingAccountStatus.fromRaw(status),
        providerStatus = providerStatus,
        customerId = customerId,
        note = note,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}

private fun FundingAccountData.toEntity(userId: String): FundingAccountEntity {
    return FundingAccountEntity(
        userId = userId,
        accountId = accountId,
        provider = provider,
        currency = currency,
        accountNumber = accountNumber,
        bankName = bankName,
        accountName = accountName,
        reference = reference,
        status = status.wireValue,
        providerStatus = providerStatus,
        customerId = customerId,
        note = note,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}

private fun sanitizeIdentifier(raw: String?): String? {
    val digits = raw?.filter(Char::isDigit).orEmpty()
    return digits.takeIf { it.isNotBlank() }
}
