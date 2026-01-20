package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressWithText(
    progress: Float,
    label: String,
    subLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(96.dp)
    ) {
        // Background ring
        CircularProgressIndicator(
            progress = { 1f },
            strokeWidth = 8.dp,
            color = Color.LightGray,
            modifier = Modifier.size(96.dp)
        )

        // Foreground ring
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            strokeWidth = 8.dp,
            color = Color(0xFF00C853), // Optional: your primary green
            modifier = Modifier.size(96.dp)
        )

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
