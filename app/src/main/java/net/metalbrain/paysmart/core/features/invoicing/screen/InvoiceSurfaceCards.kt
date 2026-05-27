package net.metalbrain.paysmart.core.features.invoicing.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

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
    val colors = PaysmartTheme.colorTokens
    return when (tone) {
        InvoiceCardTone.Default -> InvoiceCardColors(
            containerColor = colors.surfaceElevated,
            contentColor = colors.textPrimary
        )

        InvoiceCardTone.Muted -> InvoiceCardColors(
            containerColor = colors.surfacePrimary,
            contentColor = colors.textPrimary
        )

        InvoiceCardTone.Accent -> InvoiceCardColors(
            containerColor = colors.fillHover,
            contentColor = colors.textPrimary
        )

        InvoiceCardTone.Info -> InvoiceCardColors(
            containerColor = colors.buttonSecondaryBackground,
            contentColor = colors.textPrimary
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
    val colors = PaysmartTheme.colorTokens
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
        shape = PaysmartTheme.radius.medium,
        color = containerColor ?: toneColors.containerColor,
        contentColor = contentColor ?: toneColors.contentColor,
        tonalElevation = PaysmartTheme.elevation.subtle,
        shadowElevation = PaysmartTheme.elevation.none,
        border = BorderStroke(PaysmartTheme.border.thin, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
            content = content
        )
    }
}

@Composable
internal fun InvoiceSectionHeading(
    modifier: Modifier = Modifier,
    title: String,
    body: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
        Text(
            text = title,
            style = PaysmartTheme.typographyTokens.heading4,
            color = PaysmartTheme.colorTokens.textPrimary
        )
        body?.takeIf { it.isNotBlank() }?.let { copy ->
            Text(
                text = copy,
                style = PaysmartTheme.typographyTokens.bodySmall,
                color = PaysmartTheme.colorTokens.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val fieldModifier = (testTag?.let { modifier.testTag(it) } ?: modifier)
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                coroutineScope.launch {
                    delay(250)
                    bringIntoViewRequester.bringIntoView()
                }
            }
        }
        .fillMaxWidth()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = fieldModifier,
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
        shape = PaysmartTheme.radius.medium,
        textStyle = PaysmartTheme.typographyTokens.bodyMedium
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
        InvoiceNoticeTone.Neutral -> PaysmartTheme.colorTokens.surfacePrimary
        InvoiceNoticeTone.Success -> PaysmartTheme.colorTokens.fillHover
        InvoiceNoticeTone.Error -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (tone) {
        InvoiceNoticeTone.Neutral -> PaysmartTheme.colorTokens.textPrimary
        InvoiceNoticeTone.Success -> PaysmartTheme.colorTokens.textPrimary
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
            style = PaysmartTheme.typographyTokens.labelLarge,
            color = contentColor
        )
        Text(
            text = body,
            style = PaysmartTheme.typographyTokens.bodySmall,
            color = contentColor
        )
    }
}
