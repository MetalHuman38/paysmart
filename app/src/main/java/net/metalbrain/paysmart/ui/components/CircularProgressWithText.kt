package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressWithText(progress: Float, label: String, subLabel: String) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator( progress = { 1f }, strokeWidth = 8.dp, color = Color.LightGray)
        CircularProgressIndicator(progress = { progress }, strokeWidth = 8.dp, color = Color.Green)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.headlineMedium)
            Text(subLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}
