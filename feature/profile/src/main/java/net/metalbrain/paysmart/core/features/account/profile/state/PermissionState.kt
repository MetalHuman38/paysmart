package net.metalbrain.paysmart.core.features.account.profile.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun PermissionState(
    onRequestPermission: () -> Unit,
    onUseFileFallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.identity_resolver_camera_permission_required),
            style = PaysmartTheme.typographyTokens.bodyLarge,
            color = PaysmartTheme.colorTokens.textPrimary,
            textAlign = TextAlign.Center
        )
        PrimaryButton(
            text = stringResource(R.string.identity_resolver_camera_permission_action),
            onClick = onRequestPermission
        )
        OutlinedButton(
            onClick = onUseFileFallback,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.identity_resolver_capture_fallback_action),
                style = MaterialTheme.typography.labelLarge,
                color = PaysmartTheme.colorTokens.textPrimary,
            )
        }
    }
}
