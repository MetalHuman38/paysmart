package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
internal fun StartupActionCard(
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val darkMode = colorScheme.background.luminance() < 0.2f

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        color = colorScheme.surface.copy(alpha = if (darkMode) 0.88f else 0.96f),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.outline.copy(alpha = if (darkMode) 0.22f else 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            OutlinedButton(
                onClick = onCreateAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeight),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = if (darkMode) 0.42f else 0.65f),
                    contentColor = colorScheme.onSurface
                )
            ) {
                Text(stringResource(id = R.string.create_account))
            }

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeight),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text(stringResource(id = R.string.log_in))
            }
        }
    }
}
