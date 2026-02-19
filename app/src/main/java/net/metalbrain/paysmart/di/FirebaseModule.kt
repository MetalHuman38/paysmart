package net.metalbrain.paysmart.di

import dagger.Provides
import dagger.Module
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.auth.AllowFederatedLinkingPolicy
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.PassCodePolicyClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    fun provideFirebaseAppCheck(): FirebaseAppCheck {
        return FirebaseAppCheck.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun providePassCodePolicyClient(
        config: AuthApiConfig,
    ): PassCodePolicyClient {
        return PassCodePolicyClient(config)
    }

    @Provides
    fun provideAllowFederatedLinkingPolicyClient(
        config: AuthApiConfig,
    ): AllowFederatedLinkingPolicy {
        return AllowFederatedLinkingPolicy(config)
    }
}
