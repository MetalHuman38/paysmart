package net.metalbrain.paysmart.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun StatusFilterSheet(
    selected: Set<String>,
    onSelect: (Set<String>) -> Unit
) {
    val options = listOf("Successful", "In Progress", "Failed", "Cancelled")
    val state = remember { mutableStateOf(selected.toSet()) }

    Column(Modifier.padding(16.dp)) {
        Text("Select status", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        options.forEach { status ->
            val isSelected = state.value.contains(status)

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        state.value = if (isSelected) {
                            state.value - status
                        } else {
                            state.value + status
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
                Spacer(Modifier.width(8.dp))
                Text(status)
            }
        }


        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSelect(state.value) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Filter")
        }
    }
}
