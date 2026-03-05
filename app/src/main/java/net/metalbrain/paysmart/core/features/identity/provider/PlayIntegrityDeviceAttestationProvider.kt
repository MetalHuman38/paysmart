package net.metalbrain.paysmart.core.features.identity.provider

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.DeviceAttestationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayIntegrityDeviceAttestationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DeviceAttestationProvider {

    private val integrityManager by lazy { IntegrityManagerFactory.create(context) }

    override suspend fun issueAttestation(nonce: String): Result<String> = runCatching {
        require(nonce.isNotBlank()) { "Missing attestation nonce" }

        val builder = IntegrityTokenRequest.builder()
            .setNonce(nonce)
        resolveCloudProjectNumber()?.let { builder.setCloudProjectNumber(it) }

        val token = integrityManager.requestIntegrityToken(builder.build()).await().token()
        require(token.isNotBlank()) { "Play Integrity token is empty" }
        token
    }

    private fun resolveCloudProjectNumber(): Long? {
        return runCatching {
            context.getString(R.string.gcm_defaultSenderId).trim().toLongOrNull()
        }.getOrNull()
    }
}
