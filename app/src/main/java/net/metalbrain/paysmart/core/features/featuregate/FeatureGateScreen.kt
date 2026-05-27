package net.metalbrain.paysmart.core.features.featuregate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureGateScreen(
    feature: FeatureKey,
    decision: FeatureGateDecision,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val colors = PaysmartTheme.colorTokens
    Scaffold(
        containerColor = colors.backgroundPrimary,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceElevated,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary
                ),
                title = {
                    Text(text = stringResource(feature.titleResId()))
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.mediumScreenPadding, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            FeatureGateSheetCard(
                feature = feature,
                decision = decision,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = decision.nextRequirement?.toActionLabel() ?: stringResource(R.string.continue_text),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
