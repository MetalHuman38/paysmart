package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberPad(
    onDigitPressed: (Char) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf(null, '0', '<')
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        null -> Spacer(modifier = Modifier.size(64.dp))
                        '<' -> IconButton(
                            onClick = onBackspace,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                        }
                        else -> Button(
                            onClick = { onDigitPressed(key) },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape
                        ) {
                            Text(key.toString(), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}
