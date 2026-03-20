package net.metalbrain.paysmart.core.features.capabilities.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AccountLimitTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .height(3.dp)
                .fillMaxWidth()
                .background(
                    if (selected)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Transparent
                )
        )
    }
}
