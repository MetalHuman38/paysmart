package net.metalbrain.paysmart.domain.usecase


import android.app.Activity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.core.auth.providers.FacebookAuthProviderHelper
import net.metalbrain.paysmart.domain.auth.SocialAuthUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSocialAuthUseCase @Inject constructor(
    private val auth: FirebaseAuth
) : SocialAuthUseCase {

    override suspend fun signInWithGoogle(credential: AuthCredential): Result<Unit> =
        runCatching {
            auth.signInWithCredential(credential).await()
        }

    override suspend fun signInWithFacebook(activity: Activity): Result<Unit> =
        runCatching {
            val credential = FacebookAuthProviderHelper.loginWithFacebook(activity)
            auth.signInWithCredential(credential).await()
        }

    override suspend fun linkCredential(credential: AuthCredential): Result<Unit> =
        runCatching {
            val user = auth.currentUser
                ?: error("No authenticated user to link credentials")
            user.linkWithCredential(credential).await()
        }
}
