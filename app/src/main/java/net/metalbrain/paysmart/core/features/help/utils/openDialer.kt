package net.metalbrain.paysmart.core.features.help.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun openDialer(context: Context, phoneNumber: String): Boolean {
    if (phoneNumber.isBlank()) {
        return false
    }

    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = "tel:${Uri.encode(phoneNumber)}".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
