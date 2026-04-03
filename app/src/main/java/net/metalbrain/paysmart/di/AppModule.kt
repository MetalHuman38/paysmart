package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.metalbrain.paysmart.core.auth.AuthHook
import net.metalbrain.paysmart.core.auth.AuthService
import net.metalbrain.paysmart.data.repository.AuthRepository
import net.metalbrain.paysmart.data.repository.FirebaseAuthRepository
import net.metalbrain.paysmart.data.repository.FirestoreUserProfileRepository
import net.metalbrain.paysmart.core.features.account.authorization.password.repository.PasswordRepository
import net.metalbrain.paysmart.core.features.account.authorization.password.repository.SecurePasswordRepository
import net.metalbrain.paysmart.core.features.account.security.repository.SecurityRepository
import net.metalbrain.paysmart.core.features.account.security.repository.SecurityRepositoryInterface
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.core.features.account.security.manager.DefaultSecurityManager
import net.metalbrain.paysmart.domain.security.SecuritySettingsManager
import net.metalbrain.paysmart.domain.usecase.DefaultSecurityUseCase
import net.metalbrain.paysmart.domain.usecase.SecurityUseCase
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
    fun provideAuthService(
        authRepository: AuthRepository,
        hooks: List<AuthHook>
    ): AuthService {
        return AuthService(authRepository, hooks)
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
