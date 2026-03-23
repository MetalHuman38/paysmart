package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun IdentityInfoScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onHelp: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                Text(
                    text = stringResource(R.string.identity_intro_consent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                PrimaryButton(
                    text = stringResource(R.string.continue_text),
                    onClick = onContinue
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            IdentityFlowHeader(
                title = stringResource(R.string.identity_intro_title),
                subtitle = stringResource(R.string.identity_intro_subtitle),
                onBack = onBack,
                onHelp = onHelp
            )

            Text(
                text = stringResource(R.string.identity_intro_eta),
                style = MaterialTheme.typography.titleMedium,
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
        }
    }
}

private data class IdentityTipItem(
    val icon: ImageVector,
    val text: String
)

@Composable
private fun IdentityTipCard(
    title: String,
    tips: List<IdentityTipItem>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    Icon(
                        imageVector = tip.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = tip.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
