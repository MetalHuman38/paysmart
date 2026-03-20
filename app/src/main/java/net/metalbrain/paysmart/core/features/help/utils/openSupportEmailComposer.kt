package net.metalbrain.paysmart.core.features.help.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun openSupportEmailComposer(
    context: Context,
    emailAddress: String,
    subject: String,
    body: String
): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:${Uri.encode(emailAddress)}".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
