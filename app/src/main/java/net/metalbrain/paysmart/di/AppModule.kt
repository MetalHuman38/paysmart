package net.metalbrain.paysmart.di

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.Env
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.AuthHook
import net.metalbrain.paysmart.core.auth.AuthPolicyHandler
import net.metalbrain.paysmart.core.auth.AuthService
import net.metalbrain.paysmart.core.auth.PasswordPolicyHandler
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.FirebaseAuthRepository
import net.metalbrain.paysmart.data.repository.FirestoreUserProfileRepository
import net.metalbrain.paysmart.data.repository.LanguageRepositoryImpl
import net.metalbrain.paysmart.data.repository.PasswordRepository
import net.metalbrain.paysmart.data.repository.SecurePasswordRepository
import net.metalbrain.paysmart.data.repository.SecurityRepository
import net.metalbrain.paysmart.data.repository.SecurityRepositoryInterface
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.data.security.DefaultSecurityManager
import net.metalbrain.paysmart.domain.LanguageRepository
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager
import net.metalbrain.paysmart.domain.usecase.DefaultSecurityUseCase
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase
import net.metalbrain.paysmart.phone.PhoneAuthHandler
import net.metalbrain.paysmart.phone.PhoneDraft
import net.metalbrain.paysmart.phone.PhoneDraftStore
import net.metalbrain.paysmart.phone.PhoneVerifier
import net.metalbrain.paysmart.utils.AppCoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Module
    @InstallIn(SingletonComponent::class)
    object CoroutineScopeModule {

        @Provides
        @Singleton
        @AppCoroutineScope
        fun provideAppCoroutineScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }
    }

    @Provides
    @Singleton
    fun provideAuthApiConfig(): AuthApiConfig {
        return AuthApiConfig(
            baseUrl = Env.authBase,
            attachApiPrefix = false
        )
    }

    @Provides
    @Singleton
    fun provideAuthService(
        authRepository: AuthRepository,
        hooks: List<AuthHook>
    ): AuthService {
        return AuthService(authRepository, hooks)
    }

    @Provides
    @Singleton
    fun provideLanguageRepository(
        dataStore: DataStore<Preferences>
    ): LanguageRepository = LanguageRepositoryImpl(dataStore)


    @Module
    @InstallIn(SingletonComponent::class)
    object PhoneModule {

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
            return PhoneAuthHandler(
                FirebaseAuth.getInstance(),
                scope,
                state
            ).apply {
                setCallbacks(
                    onCodeSent = { Log.d("PhoneAuth", "Code sent!") },
                    onError = { Log.e("PhoneAuth", "Error: ${it.message}") }
                )
            }
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object AuthPolicyModule {

        @Provides
        @Singleton
        fun provideAuthPolicyHandler(): AuthPolicyHandler {
            return AuthPolicyHandler()
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object PasswordPolicyModule {

        @Provides
        @Singleton
        fun providePasswordPolicyHandler(): PasswordPolicyHandler {
            return PasswordPolicyHandler()
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class RepositoryModule {

        @Binds
        abstract fun bindUserProfileRepository(
            impl: FirestoreUserProfileRepository
        ): UserProfileRepository

        @Binds
        abstract fun bindAuthRepository(
            impl: FirebaseAuthRepository
        ): AuthRepository
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object SecurityModule {

        @Module
        @InstallIn(SingletonComponent::class)
        abstract class PasswordBindingModule {

            @Binds
            abstract fun bindPasswordRepo(
                impl: SecurePasswordRepository
            ): PasswordRepository
        }

        @Module
        @InstallIn(SingletonComponent::class)
        abstract class UseCaseBindingModule {

            @Binds
            @Singleton
            abstract fun bindSecurityUseCase(
                impl: DefaultSecurityUseCase
            ): SecurityUseCase
        }

        @Module
        @InstallIn(SingletonComponent::class)
        abstract class SecurityBindingModule {
            @Binds
            abstract fun bindSecurityManager(
                impl: DefaultSecurityManager
            ): SecuritySettingsManager

            @Binds
            abstract fun bindSecurityRepository(
                impl: SecurityRepository
            ): SecurityRepositoryInterface
        }
    }

}
