package net.metalbrain.paysmart.core.features.identity.provider

import net.metalbrain.paysmart.BuildConfig
import net.metalbrain.paysmart.core.features.identity.uploadhelpers.DeviceAttestationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurableDeviceAttestationProvider @Inject constructor(
    private val fallbackProvider: FallbackDeviceAttestationProvider,
    private val playIntegrityProvider: PlayIntegrityDeviceAttestationProvider
) : DeviceAttestationProvider {

    override suspend fun issueAttestation(nonce: String): Result<String> {
        return if (BuildConfig.IS_LOCAL) {
            fallbackProvider.issueAttestation(nonce)
        } else {
            playIntegrityProvider.issueAttestation(nonce)
        }
    }
}
