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
import net.metalbrain.paysmart.domain.model.SecuritySettings
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.account.AccountsHeader
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun HomeContent(
    security: SecuritySettings,
    onProfileClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
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
        if (!security.hasCompletedEmailVerification ||
            !security.hasCompletedAddress ||
            !security.hasCompletedIdentity) {
            item { ProfileCompletionCard(
                security = security,
                onVerifyEmailClick = onVerifyEmailClick,
                onAddAddressClick = onAddAddressClick,
                onVerifyIdentityClick = onVerifyIdentityClick,
            )
            }
        }

        item {
            AccountsHeader(
                isBalanceVisible = showBalance.value,
                onToggleVisibility = { showBalance.value = !showBalance.value }
            )
        }

    }
}
