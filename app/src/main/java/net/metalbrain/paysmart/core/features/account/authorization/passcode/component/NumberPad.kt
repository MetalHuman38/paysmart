package net.metalbrain.paysmart.core.features.account.authorization.passcode.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun NumberPad(
    onDigitPressed: (Char) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf(null, '0', '<')
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    when (key) {
                        null -> Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        )

                        '<' -> NumberPadKey(
                            modifier = Modifier.weight(1f),
                            onClick = onBackspace
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.content_desc_backspace)
                            )
                        }

                        else -> NumberPadKey(
                            modifier = Modifier.weight(1f),
                            onClick = { onDigitPressed(key) }
                        ) {
                            Text(
                                text = key.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
