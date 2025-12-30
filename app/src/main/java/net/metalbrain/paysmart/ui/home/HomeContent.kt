package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.hasAddedHomeAddress
import net.metalbrain.paysmart.domain.model.hasVerifiedEmail
import net.metalbrain.paysmart.domain.model.hasVerifiedIdentity
import net.metalbrain.paysmart.ui.account.AccountsHeader
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun HomeContent(
    user: AuthUserModel,
    onProfileClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
    viewModel: UserViewModel
) {

    val showBalance = remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.mediumScreenPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        item {
            HomeTopBarContainer(
                onProfileClick = onProfileClick,
                onReferralClick = { /* show referral screen */ }
            )
        }



        // 🧩 Profile Completion Card (only if needed)
        if (!user.hasVerifiedEmail || !user.hasAddedHomeAddress || !user.hasVerifiedIdentity) {
            item { ProfileCompletionCard(
                user = user,
                onVerifyEmailClick = onVerifyEmailClick,
                onAddAddressClick = onAddAddressClick,
                onVerifyIdentityClick = onVerifyIdentityClick,
                viewModel = viewModel

            )
            }
        }

        item {
            AccountsHeader(
                isBalanceVisible = showBalance.value,
                onToggleVisibility = { showBalance.value = !showBalance.value }
            )
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
