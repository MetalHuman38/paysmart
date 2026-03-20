package net.metalbrain.paysmart.core.features.help.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun openExternalUri(context: Context, uri: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
