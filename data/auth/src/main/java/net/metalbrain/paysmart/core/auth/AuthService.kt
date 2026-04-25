package net.metalbrain.paysmart.core.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.data.repository.AuthProvider
import net.metalbrain.paysmart.data.repository.AuthRepository

class AuthService(
    private val repo: AuthRepository,
    private val hooks: List<AuthHook> = emptyList()
) {

    suspend fun signIn(provider: AuthProvider): AuthResult {
        var credential: AuthCredential? = null

        try {
            credential = provider.getCredential()
        } catch (e: Exception) {
            throw e
        }

        // 1. Run before-sign-in hooks
        val signInAllowed = runBeforeSignIn(provider, credential)
        if (signInAllowed == HookDecision.Deny) {
            throw IllegalStateException("Sign-in blocked by hook policy.")
        }

        // 2. Call FirebaseAuth via repository
        val userCred = repo.signInWithCredential(credential)

        // 3. If new user, run before-create hook
        val isNewUser = userCred.additionalUserInfo?.isNewUser == true
        if (isNewUser) {
            val createAllowed = runBeforeCreateUser(userCred)
            if (createAllowed == HookDecision.Deny) {
                try {
                    userCred.user?.delete()
                } catch (_: Exception) {
                }
                throw IllegalStateException("Account creation blocked by hook policy.")
            }
        }

        // 4. After sign-in hooks
        runAfterSignIn(userCred)

        return userCred
    }

    suspend fun link(provider: AuthProvider) {
        val currentUser = repo.currentUser ?: throw IllegalStateException("No user to link.")
        val credential = provider.getCredential()
        currentUser.linkWithCredential(credential).await()
    }

    suspend fun signOut() {
        repo.signOut()
    }

    // Hook runners
    private suspend fun runBeforeSignIn(
        provider: AuthProvider,
        credential: AuthCredential?
    ): HookDecision {
        val context = HookContext(
            providerId = provider.providerId,
            email = extractEmail(provider, credential),
            oauthIdToken = extractIdToken(credential),
            oauthAccessToken = extractAccessToken(credential)
        )

        for (hook in hooks) {
            val decision = hook.beforeSignIn(context)
            if (decision == HookDecision.Deny) return HookDecision.Deny
        }

        return HookDecision.Allow
    }

    private suspend fun runBeforeCreateUser(userCred: AuthResult): HookDecision {
        for (hook in hooks) {
            val decision = hook.beforeCreateUser(userCred)
            if (decision == HookDecision.Deny) return HookDecision.Deny
        }
        return HookDecision.Allow
    }

    private suspend fun runAfterSignIn(userCred: AuthResult) {
        for (hook in hooks) {
            hook.afterSignIn(userCred)
        }
    }

    private fun extractEmail(provider: AuthProvider, credential: AuthCredential?): String? {
        // You can inspect your provider type if needed
        return null
    }

    private fun extractIdToken(credential: AuthCredential?): String? {
        // Not exposed directly in most providers
        return null
    }

    private fun extractAccessToken(credential: AuthCredential?): String? {
        // Not exposed directly in most providers
        return null
    }
}
