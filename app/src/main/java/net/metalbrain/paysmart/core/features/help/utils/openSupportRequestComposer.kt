package net.metalbrain.paysmart.core.features.help.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openSupportRequestComposer(
    context: Context,
    emailAddress: String,
    subject: String,
    body: String,
    attachmentUri: Uri?
): Boolean {
    if (attachmentUri == null) {
        return openSupportEmailComposer(
            context = context,
            emailAddress = emailAddress,
            subject = subject,
            body = body
        )
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = context.contentResolver.getType(attachmentUri) ?: "image/*"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        putExtra(Intent.EXTRA_STREAM, attachmentUri)
        clipData = ClipData.newUri(context.contentResolver, "support_attachment", attachmentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(sendIntent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return try {
        context.startActivity(chooserIntent)
        true
    } catch (_: ActivityNotFoundException) {
        openSupportEmailComposer(
            context = context,
            emailAddress = emailAddress,
            subject = subject,
            body = body
        )
    }
}
