package net.metalbrain.paysmart.core.auth.providers

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.metalbrain.paysmart.R

object GoogleAuthProvider {

    suspend fun getGoogleCredential(
        context: Context,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
    ): AuthCredential = withContext(Dispatchers.IO) {
        val credentialManager = CredentialManager.create(context)

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val token = googleIdTokenCredential.idToken
                return@withContext GoogleAuthProvider.getCredential(token, null)
            }

            throw IllegalStateException("Unexpected credential type: ${credential::class.simpleName}")

        } catch (e: NoCredentialException) {
            launcher?.launch(Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            })
            throw e
        } catch (e: GetCredentialException) {
            throw e
        }
    }
}
