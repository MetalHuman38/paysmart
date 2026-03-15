package net.metalbrain.paysmart.core.features.referral.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.referral.model.ReferralRewardCatalog

@Composable
fun ReferralBannerButton(
    countryIso2: String,
    countryCurrencyCode: String,
    onClick: () -> Unit
) {
    val reward = remember(countryIso2, countryCurrencyCode) {
        ReferralRewardCatalog.rewardFor(
            countryIso2 = countryIso2,
            currencyCode = countryCurrencyCode
        )
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0D6DF)),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.CardGiftcard,
            contentDescription = stringResource(R.string.content_desc_gift),
            tint = Color(0xFF1B9C85),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(
                R.string.referral_banner_reward_text,
                reward.amount,
                reward.currencyCode
            ),
            color = Color(0xFF1B9C85),
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}
