package net.metalbrain.paysmart.core.features.account.creation.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.account.creation.data.SecurityStepSpec
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

/**
 * A composable card that displays a specific security step or feature information,
 * typically shown during the account creation flow after OTP verification.
 *
 * The card consists of a title followed by a row containing a themed icon and
 * a detailed description.
 *
 * @param step The [SecurityStepSpec] containing the title, description, and icon to be displayed.
 * @param modifier The [Modifier] to be applied to the card's container.
 */
@Composable
fun PostOtpSecurityStepCard(
    step: SecurityStepSpec,
    modifier: Modifier = Modifier
) {
    val colors = PaysmartTheme.colorTokens

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.xs),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        Text(
            text = step.title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.xl)
                    .background(
                        color = colors.fillHover,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = colors.brandPrimary,
                    modifier = Modifier.size(Dimens.md)
                )
            }

            Text(
                text = step.description,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp)
                    .heightIn(min = 36.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}
