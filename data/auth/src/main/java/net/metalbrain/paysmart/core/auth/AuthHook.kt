package net.metalbrain.paysmart.core.auth
import com.google.firebase.auth.AuthResult

enum class HookDecision {
    Allow, Deny
}

data class HookContext(
    val providerId: String,
    val email: String?,
    val oauthIdToken: String?,
    val oauthAccessToken: String?
)

interface AuthHook {
    suspend fun beforeSignIn(context: HookContext): HookDecision
    suspend fun beforeCreateUser(credential: AuthResult): HookDecision
    suspend fun afterSignIn(credential: AuthResult)
}
