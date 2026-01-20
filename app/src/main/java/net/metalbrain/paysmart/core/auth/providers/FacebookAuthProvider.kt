package net.metalbrain.paysmart.core.auth.providers


import android.app.Activity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import kotlinx.coroutines.CompletableDeferred

object FacebookAuthProviderHelper {

    private val callbackManager: CallbackManager = CallbackManager.Factory.create()

    fun getCallbackManager(): CallbackManager = callbackManager

    suspend fun loginWithFacebook(
        activity: Activity,
        permissions: List<String> = listOf("public_profile", "email")
    ): AuthCredential {
        val deferred = CompletableDeferred<AuthCredential>()

        LoginManager.getInstance().logInWithReadPermissions(activity, permissions)

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken.token
                val credential = FacebookAuthProvider.getCredential(token)
                deferred.complete(credential)
            }

            override fun onCancel() {
                deferred.completeExceptionally(Exception("Facebook login cancelled"))
            }

            override fun onError(error: FacebookException) {
                deferred.completeExceptionally(error)
            }
        })

        return deferred.await()
    }

    fun isUserLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    fun logout() {
        LoginManager.getInstance().logOut()
    }
}
