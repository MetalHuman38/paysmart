package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun PostOtpCapabilityList(
    items: List<CapabilityItem>,
    onItemClick: (CapabilityItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            vertical = Dimens.md,
            horizontal = Dimens.screenPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = items,
            key = { it.key }
        ) { item ->

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                PostOtpCapabilityRow(
                    item = item,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.animateContentSize() // safer
                )
            }
        }
    }
}
