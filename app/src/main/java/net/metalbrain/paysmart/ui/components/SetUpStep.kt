package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun SetupStep(
    label: String,
    done: Boolean,
    onClick: (() -> Unit)? = null
) {
    val textStyle = if (done) {
        MaterialTheme.typography.bodyMedium.copy(
            textDecoration = TextDecoration.LineThrough,
            color = Color.Gray
        )
    } else {
        MaterialTheme.typography.bodyMedium
    }

    val iconColor = if (done) Color(0xFF4CAF50) else Color.Gray
    val icon = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked

    val rowModifier = if (onClick != null && !done) {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = textStyle
        )
    }
}
