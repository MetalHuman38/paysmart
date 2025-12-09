package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.AuthUserModel

@Composable
fun HomeContent(user: AuthUserModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        // 🧩 Profile Completion Card (only if needed)
        if (!user.hasVerifiedEmail || !user.hasAddedHomeAddress || !user.hasVerifiedIdentity) {
            item {
                ProfileCompletionCard(
                    user = user,
                    onAddAddressClick = { /* Navigate */ },
                    onVerifyIdentityClick = { /* Navigate */ }
                )
            }
        }

        // 💰 My Accounts Section
        item {
            AccountInfoSection()
        }

        // 🔁 Transactions Placeholder
        item {
            TransactionsSection()
        }

        // 🔁 Exchange Rate Mock Section
        item {
            ExchangeRateCard()
        }
    }
}
