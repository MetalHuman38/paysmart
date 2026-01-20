package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FilterTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) Color.Black else Color.LightGray,
        onClick = onClick
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}
