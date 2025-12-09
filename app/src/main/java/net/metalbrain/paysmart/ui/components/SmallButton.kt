package net.metalbrain.paysmart.ui.components


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SmallTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.Cyan,
    cornerRadius: Int = 20
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.dp),
        contentPadding = ButtonDefaults.TextButtonContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        content = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    )
}
