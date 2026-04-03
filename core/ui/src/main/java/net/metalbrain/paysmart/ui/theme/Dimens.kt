package net.metalbrain.paysmart.ui.theme

import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.tokens.DSHeightTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSSpacingTokens
import net.metalbrain.paysmart.ui.theme.tokens.DSWidthTokens

/**
 * Compatibility layer over the DS token objects.
 *
 * Existing feature code can continue to use [Dimens] without change.
 * New code should use [PaysmartTheme.spacing], [PaysmartTheme.width],
 * and [PaysmartTheme.height] token accessors instead.
 */
object Dimens {
    // Standard spacing — backed by DSSpacingTokens
    val xs get() = DSSpacingTokens.xs
    val sm get() = DSSpacingTokens.sm
    val md get() = DSSpacingTokens.md
    val lg get() = DSSpacingTokens.lg
    val xl get() = DSSpacingTokens.xl
    val xxl get() = DSSpacingTokens.xxl

    // Width — backed by DSWidthTokens
    val widthX get() = DSWidthTokens.compact
    val widthY get() = DSWidthTokens.medium
    val widthZ get() = DSWidthTokens.wide

    // Height — backed by DSHeightTokens
    val heightX get() = DSHeightTokens.compact
    val heightY get() = DSHeightTokens.medium

    // Legacy space* aliases
    val space2 get() = DSSpacingTokens.xs
    val space4 get() = DSSpacingTokens.sm
    val space6 = 12.dp
    val space8 get() = DSSpacingTokens.md
    val space10 = 20.dp
    val space12 get() = DSSpacingTokens.lg
    val space16 get() = DSSpacingTokens.xl

    // Screen paddings
    val screenPadding get() = DSSpacingTokens.lg
    val smallScreenPadding get() = DSSpacingTokens.sm
    val mediumScreenPadding get() = DSSpacingTokens.md
    val largeScreenPadding get() = DSSpacingTokens.xl

    // Legacy named aliases
    val smallSpacing get() = DSSpacingTokens.sm
    val mediumSpacing get() = DSSpacingTokens.md
    val largeSpacing get() = DSSpacingTokens.xl

    // Shared controls — backed by DSHeightTokens / DSRadiusTokens
    val buttonHeight get() = DSHeightTokens.button
    val cornerRadius = 14.dp
    val minimumTouchTarget get() = DSHeightTokens.minimumTouchTarget
}

object ScreenDimensions {
    val smallSpacing get() = Dimens.smallSpacing
    val mediumSpacing get() = Dimens.mediumSpacing
}

object HeightDimensions {
    val buttonHeight get() = Dimens.buttonHeight
    val cornerRadius get() = Dimens.cornerRadius
    val largeSpacing get() = Dimens.largeSpacing
    val mediumSpacing get() = Dimens.mediumSpacing
    val smallSpacing get() = Dimens.smallSpacing
}

object CardDimensions {
    val smallCardHeight = 100.dp
    val mediumCardHeight = 150.dp
    val largeCardHeight = 200.dp
    val largeCardPadding get() = Dimens.space12
    val smallCardPadding get() = Dimens.space8
    val mediumCardPadding get() = Dimens.space10
}
