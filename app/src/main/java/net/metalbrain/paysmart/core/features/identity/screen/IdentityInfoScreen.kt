package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.component.IdentityTipCard
import net.metalbrain.paysmart.core.features.identity.data.IdentityTipItem
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun IdentityInfoScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onHelp: () -> Unit
) {
    val typography = PaysmartTheme.typographyTokens
    val color = PaysmartTheme.colorTokens
    Scaffold(
        containerColor = color.backgroundPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            IdentityFlowHeader(
                title = stringResource(R.string.identity_intro_title),
                subtitle = stringResource(R.string.identity_intro_subtitle),
                onBack = onBack,
                onHelp = onHelp
            )

            Text(
                text = stringResource(R.string.identity_intro_eta),
                style = typography.heading4,
                color = color.textPrimary,
                fontWeight = FontWeight.SemiBold
            )

            IdentityTipCard(
                title = stringResource(R.string.identity_intro_photo_tips_title),
                tips = listOf(
                    IdentityTipItem(
                        icon = Icons.Default.PhotoCamera,
                        text = stringResource(R.string.identity_intro_photo_tip_readable)
                    ),
                    IdentityTipItem(
                        icon = Icons.Default.CreditCard,
                        text = stringResource(R.string.identity_intro_photo_tip_visible)
                    )
                )
            )

            IdentityTipCard(
                title = stringResource(R.string.identity_intro_selfie_tips_title),
                tips = listOf(
                    IdentityTipItem(
                        icon = Icons.Default.WbSunny,
                        text = stringResource(R.string.identity_intro_selfie_tip_lighting)
                    ),
                    IdentityTipItem(
                        icon = Icons.Default.Visibility,
                        text = stringResource(R.string.identity_intro_selfie_tip_clear)
                    )
                )
            )

            Text(
                text = stringResource(R.string.identity_intro_consent),
                style = typography.bodySmall,
                color = color.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = stringResource(R.string.continue_text),
                onClick = onContinue
            )

            Spacer(modifier = Modifier.height(Dimens.xl))
        }
    }
}
