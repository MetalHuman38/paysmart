package net.metalbrain.paysmart.core.auth.providers

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleAuthProvider {

    suspend fun getGoogleCredential(
        context: Context,
        clientId: String,
        intent: GoogleAuthIntent,
        onNoAccountFound: ((Intent) -> Unit)? = null
    ): AuthCredential = withContext(Dispatchers.IO) {
        val credentialManager = CredentialManager.create(context)

        Log.d(
            "GoogleCredential",
            "request_start intent=$intent package=${context.packageName} clientIdSuffix=${clientId.takeLast(16)}"
        )

        val request = buildRequest(clientId = clientId, intent = intent)

        try {
            val credential = credentialManager.getCredential(context, request).credential

            Log.d(
                "GoogleCredential",
                "request_success intent=$intent credentialType=${credential::class.simpleName}"
            )

            return@withContext credential.toFirebaseAuthCredential()
        } catch (e: NoCredentialException) {
            if (intent == GoogleAuthIntent.LINK) {
                Log.d(
                    "GoogleCredential",
                    "link_no_credential_fallback_to_sign_in package=${context.packageName}"
                )
                try {
                    val interactiveCredential = credentialManager
                        .getCredential(
                            context,
                            buildRequest(clientId = clientId, intent = GoogleAuthIntent.SIGN_IN)
                        )
                        .credential

                    Log.d(
                        "GoogleCredential",
                        "link_fallback_success credentialType=${interactiveCredential::class.simpleName}"
                    )

                    return@withContext interactiveCredential.toFirebaseAuthCredential()
                } catch (fallbackError: GetCredentialException) {
                    Log.e(
                        "GoogleCredential",
                        "link_fallback_failed type=${fallbackError::class.simpleName} message=${fallbackError.message}",
                        fallbackError
                    )
                    if (fallbackError is NoCredentialException) {
                        onNoAccountFound?.invoke(Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                        })
                    }
                    throw fallbackError
                }
            }

            Log.w(
                "GoogleCredential",
                "no_credential intent=$intent package=${context.packageName} launchingAddAccount=${onNoAccountFound != null}",
                e
            )
            onNoAccountFound?.invoke(Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            })
            throw e
        } catch (e: GetCredentialException) {
            Log.e(
                "GoogleCredential",
                "request_failed intent=$intent type=${e::class.simpleName} message=${e.message}",
                e
            )
            throw e
        }
    }

    private fun buildRequest(
        clientId: String,
        intent: GoogleAuthIntent,
    ): GetCredentialRequest =
        when (intent) {
            GoogleAuthIntent.SIGN_IN ->
                GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetSignInWithGoogleOption.Builder(clientId)
                            .build()
                    )
                    .build()

            GoogleAuthIntent.LINK ->
                GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setServerClientId(clientId)
                            .setAutoSelectEnabled(false)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()
        }

    private fun Credential.toFirebaseAuthCredential(): AuthCredential {
        if (
            this is CustomCredential &&
            (
                type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                    type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                )
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(data)
            return GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        }

        throw IllegalStateException("Unexpected credential type: ${this::class.simpleName}")
    }
}
