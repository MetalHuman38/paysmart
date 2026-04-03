package net.metalbrain.paysmart.core.features.account.authorization.passcode.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun NumberPadKey(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val editorialLayout = securityStyle.useEditorialLayout
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(if (editorialLayout) 22.dp else Dimens.cornerRadius + 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (editorialLayout) {
                MaterialTheme.colorScheme
                    .surfaceColorAtElevation(18.dp)
                    .copy(alpha = 0.98f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
            },
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (editorialLayout) {
                MaterialTheme.colorScheme.outline.copy(
                    alpha = securityStyle.ghostBorderAlpha + 0.12f
                )
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        content()
    }
}
