package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.state.HomeNotificationUiState
import net.metalbrain.paysmart.ui.home.state.HomeTransactionProviderFilter
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun HomeTopBarContainer(
    onProfileClick: () -> Unit,
    onReferralClick: () -> Unit,
    displayName: String,
    countryIso2: String,
    countryCurrencyCode: String,
    transactionSearchQuery: String,
    availableTransactionProviders: List<HomeTransactionProviderFilter>,
    selectedTransactionProviders: Set<HomeTransactionProviderFilter>,
    notification: HomeNotificationUiState,
    onTransactionSearchQueryChange: (String) -> Unit,
    onTransactionProviderToggle: (HomeTransactionProviderFilter) -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val actionGroupWidth = (Dimens.minimumTouchTarget * 2) + Dimens.xs

    LaunchedEffect(transactionSearchQuery, selectedTransactionProviders) {
        if (transactionSearchQuery.isNotBlank() || selectedTransactionProviders.isNotEmpty()) {
            isSearchExpanded = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(actionGroupWidth),
                contentAlignment = Alignment.CenterStart
            ) {
                HomeHeaderIconButton(
                    icon = Icons.Default.PersonOutline,
                    contentDescription = stringResource(R.string.profile_title),
                    onClick = onProfileClick
                )
            }

            Text(
                text = displayName.ifBlank { stringResource(R.string.app_name) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.sm),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.width(actionGroupWidth),
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeHeaderIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = stringResource(R.string.home_top_bar_search_content_description),
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                    }
                )
                HomeHeaderIconButton(
                    icon = Icons.Default.NotificationsNone,
                    contentDescription = stringResource(R.string.home_top_bar_notifications_content_description),
                    badgeCount = notification.unreadCount,
                    showIndicator = notification.isUnread,
                    onClick = onNotificationClick
                )
            }
        }

        if (isSearchExpanded) {
            HomeTransactionSearchPanel(
                searchQuery = transactionSearchQuery,
                availableProviders = availableTransactionProviders,
                selectedProviders = selectedTransactionProviders,
                onSearchQueryChange = onTransactionSearchQueryChange,
                onProviderToggle = onTransactionProviderToggle
            )
        }

//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End
//        ) {
//            ReferralBannerButton(
//                countryIso2 = countryIso2,
//                countryCurrencyCode = countryCurrencyCode,
//                onClick = onReferralClick
//            )
//        }
    }
}
