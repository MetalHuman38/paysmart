package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import net.metalbrain.paysmart.ui.theme.Dimens

internal enum class InvoiceCardTone {
    Default,
    Muted,
    Accent,
    Info
}

internal enum class InvoiceNoticeTone {
    Neutral,
    Success,
    Error
}

@Immutable
private data class InvoiceCardColors(
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun rememberInvoiceCardColors(tone: InvoiceCardTone): InvoiceCardColors {
    return when (tone) {
        InvoiceCardTone.Default -> InvoiceCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        InvoiceCardTone.Muted -> InvoiceCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        InvoiceCardTone.Accent -> InvoiceCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        InvoiceCardTone.Info -> InvoiceCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
internal fun InvoiceSurfaceCard(
    modifier: Modifier = Modifier,
    tone: InvoiceCardTone = InvoiceCardTone.Default,
    containerColor: Color? = null,
    contentColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val toneColors = rememberInvoiceCardColors(tone)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick == null) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            ),
        shape = MaterialTheme.shapes.medium,
        color = containerColor ?: toneColors.containerColor,
        contentColor = contentColor ?: toneColors.contentColor
    ) {
        Column(
            modifier = Modifier.padding(Dimens.space6),
            verticalArrangement = Arrangement.spacedBy(Dimens.xs),
            content = content
        )
    }
}

@Composable
internal fun InvoiceSectionHeading(
    modifier: Modifier = Modifier,
    title: String,
    body: String? = null,

) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        body?.takeIf { it.isNotBlank() }?.let { copy ->
            Text(
                text = copy,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.76f)
            )
        }
    }
}

@Composable
internal fun InvoiceInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable (() -> Unit))? = null,
    testTag: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = (testTag?.let { modifier.testTag(it) } ?: modifier).fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { hint -> ({ Text(hint) }) },
        supportingText = supportingText?.let { copy -> ({ Text(copy) }) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        maxLines = if (singleLine) 1 else 4,
        readOnly = readOnly,
        enabled = enabled,
        isError = isError,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
internal fun InvoiceGuideCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    InvoiceSurfaceCard(
        modifier = modifier,
        tone = InvoiceCardTone.Info
    ) {
        InvoiceSectionHeading(
            title = title,
            body = body
        )
    }
}

@Composable
internal fun InvoiceNoticeCard(
    title: String,
    body: String,
    tone: InvoiceNoticeTone,
    modifier: Modifier = Modifier
) {
    val containerColor = when (tone) {
        InvoiceNoticeTone.Neutral -> MaterialTheme.colorScheme.surfaceContainer
        InvoiceNoticeTone.Success -> MaterialTheme.colorScheme.secondaryContainer
        InvoiceNoticeTone.Error -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (tone) {
        InvoiceNoticeTone.Neutral -> MaterialTheme.colorScheme.onSurface
        InvoiceNoticeTone.Success -> MaterialTheme.colorScheme.onSecondaryContainer
        InvoiceNoticeTone.Error -> MaterialTheme.colorScheme.onErrorContainer
    }

    InvoiceSurfaceCard(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tone = InvoiceCardTone.Default
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = contentColor
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    }
}
