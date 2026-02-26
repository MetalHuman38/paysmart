package net.metalbrain.paysmart.core.features.identity.provider

import android.util.Base64
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.DeviceAttestationProvider
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FallbackDeviceAttestationProvider @Inject constructor() : DeviceAttestationProvider {
    override suspend fun issueAttestation(nonce: String): Result<String> = runCatching {
        // TODO: Replace with Play Integrity / hardware-backed attestation.
        val payload = JSONObject()
            .put("nonce", nonce)
            .put("issuedAt", System.currentTimeMillis())
            .put("provider", "fallback")
            .toString()
        val encoded = Base64.encodeToString(
            payload.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
        "fallback.$encoded.signature"
    }
}
