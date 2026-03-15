package net.metalbrain.paysmart.core.features.fundingaccount.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundingAccountScreen(
    state: FundingAccountUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onProvision: () -> Unit,
    onCopyAccountNumber: () -> Unit,
    onShareDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.funding_account_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        FundingAccountContent(
            state = state,
            contentPadding = innerPadding,
            onRefresh = onRefresh,
            onProvision = onProvision,
            onCopyAccountNumber = onCopyAccountNumber,
            onShareDetails = onShareDetails
        )
    }
}
