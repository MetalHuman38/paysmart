package net.metalbrain.paysmart.core.auth

data class AuthApiConfig(
    val baseUrl: String,
    val beforeCreatePath: String = "/auth/beforeCreate",
    val beforeSignInPath: String = "/auth/beforeSignIn",
    val usersEnsurePath: String = "/users/ensure",
    val attachApiPrefix: Boolean = false
) {
    val apiBase: String
        get() {
            val root = baseUrl.trimEnd('/')
            return if (attachApiPrefix && !root.endsWith("/api")) "$root/api" else root
        }

    val beforeCreateUrl get() = "$apiBase$beforeCreatePath"
    val beforeSignInUrl get() = "$apiBase$beforeSignInPath"
    val usersEnsureUrl get() = "$apiBase$usersEnsurePath"
}
