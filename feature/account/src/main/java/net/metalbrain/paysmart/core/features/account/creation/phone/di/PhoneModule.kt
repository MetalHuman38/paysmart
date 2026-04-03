package net.metalbrain.paysmart.core.features.account.creation.phone.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.common.runtime.RuntimeConfig
import net.metalbrain.paysmart.core.features.account.creation.phone.core.PhoneAuthHandler
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneDraft
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneDraftStore
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneVerifier
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PluggablePhoneVerifier
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PnvPreviewPhoneVerifier

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PhoneModule {
    @Provides
    @Singleton
    fun providePhoneVerifier(
        phoneDraftStore: PhoneDraftStore,
        runtimeConfig: RuntimeConfig
    ): PhoneVerifier {
        val state = MutableStateFlow(PhoneDraft())
        val scope = CoroutineScope(Dispatchers.Main)

        // Sync latest draft to flow
        scope.launch {
            phoneDraftStore.draft.collect {
                state.value = it
            }
            Log.d("PhoneAuth", "Phone verifier initialized")
        }
        val legacyVerifier = PhoneAuthHandler(
            FirebaseAuth.getInstance(),
            scope,
            state,
            phoneDraftStore
        )
        val pnvVerifier = PnvPreviewPhoneVerifier(legacyVerifier)
        return PluggablePhoneVerifier(
            legacyVerifier = legacyVerifier,
            pnvVerifier = pnvVerifier,
            pnvPreviewEnabled = runtimeConfig.phonePnvPreviewEnabled
        ).apply {
            setCallbacks(
                onCodeSent = { Log.d("PhoneAuth", "Code sent!") },
                onError = { Log.e("PhoneAuth", "Error: ${it.message}") }
            )
        }
    }
}
