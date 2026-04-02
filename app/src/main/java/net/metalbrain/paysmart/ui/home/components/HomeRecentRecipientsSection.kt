package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.ui.home.card.HomeRecentRecipientCard
import net.metalbrain.paysmart.ui.home.state.HomeRecentRecipient
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun HomeRecentRecipientsSection(
    recipients: List<HomeRecentRecipient>,
    preferredCurrencyCode: String,
    preferredFlagEmoji: String,
    onRecipientClick: (HomeRecentRecipient) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.xs),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        items(
            items = recipients,
            key = { recipient -> recipient.recipientKey }
        ) { recipient ->
            HomeRecentRecipientCard(
                recipient = recipient,
                preferredCurrencyCode = preferredCurrencyCode,
                preferredFlagEmoji = preferredFlagEmoji,
                onClick = { onRecipientClick(recipient) }
            )
        }
    }
}
