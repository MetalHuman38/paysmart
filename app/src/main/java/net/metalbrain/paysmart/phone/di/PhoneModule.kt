package net.metalbrain.paysmart.phone.di

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
import net.metalbrain.paysmart.phone.core.PhoneAuthHandler
import net.metalbrain.paysmart.phone.data.PhoneDraft
import net.metalbrain.paysmart.phone.data.PhoneDraftStore
import net.metalbrain.paysmart.phone.data.PhoneVerifier
import net.metalbrain.paysmart.phone.data.PluggablePhoneVerifier
import net.metalbrain.paysmart.phone.data.PnvPreviewPhoneVerifier
import net.metalbrain.paysmart.Env

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PhoneModule {
    private val pnvPreviewEnabled = Env.pvn

    @Provides
    @Singleton
    fun providePhoneVerifier(
        phoneDraftStore: PhoneDraftStore
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
            state
        )
        val pnvVerifier = PnvPreviewPhoneVerifier(legacyVerifier)
        return PluggablePhoneVerifier(
            legacyVerifier = legacyVerifier,
            pnvVerifier = pnvVerifier,
            pnvPreviewEnabled = pnvPreviewEnabled
        ).apply {
            setCallbacks(
                onCodeSent = { Log.d("PhoneAuth", "Code sent!") },
                onError = { Log.e("PhoneAuth", "Error: ${it.message}") }
            )
        }
    }
}
