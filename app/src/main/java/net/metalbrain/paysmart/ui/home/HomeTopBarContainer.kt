package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.profile.UserIconButton
import net.metalbrain.paysmart.ui.referral.ReferralBannerButton

@Composable
fun HomeTopBarContainer(
    onProfileClick: () -> Unit,
    onReferralClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserIconButton(onClick = onProfileClick)
        ReferralBannerButton(onClick = onReferralClick)
    }
}
