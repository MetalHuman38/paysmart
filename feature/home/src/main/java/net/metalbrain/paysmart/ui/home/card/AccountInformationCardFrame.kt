package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.HomeCardTokens

@Composable
fun AccountInformationCardFrame(
    modifier: Modifier = Modifier,
    gradient: Brush,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(HomeCardTokens.accountInfoCardHeight),
        shape = HomeCardTokens.cardShape,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = HomeCardTokens.OUTLINE_ALPHA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeCardTokens.subtleElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(HomeCardTokens.compactContentPadding)
        ) {
            content()
        }
    }
}

