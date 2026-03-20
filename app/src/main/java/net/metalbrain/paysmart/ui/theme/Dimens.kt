package net.metalbrain.paysmart.ui.theme

import androidx.compose.ui.unit.dp

object Dimens {
    // Standard spacing tokens for new UI work.
    val xs = 6.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp

    // Legacy aliases kept for compatibility during migration.
    val space2 = xs
    val space4 = sm
    val space6 = 12.dp
    val space8 = md
    val space10 = 20.dp
    val space12 = lg
    val space16 = xl

    // Screen paddings
    val screenPadding = lg
    val smallScreenPadding = sm
    val mediumScreenPadding = md
    val largeScreenPadding = xl

    // Legacy aliases kept for compatibility
    val smallSpacing = space4
    val mediumSpacing = space8
    val largeSpacing = space16

    // Shared controls
    val buttonHeight = 52.dp
    val cornerRadius = 14.dp
    val minimumTouchTarget = 48.dp
}

object ScreenDimensions {
    val smallSpacing = Dimens.smallSpacing
    val mediumSpacing = Dimens.mediumSpacing
}

object HeightDimensions {
    val buttonHeight = Dimens.buttonHeight
    val cornerRadius = Dimens.cornerRadius
    val largeSpacing = Dimens.largeSpacing
    val mediumSpacing = Dimens.mediumSpacing
    val smallSpacing = Dimens.smallSpacing
}

object CardDimensions {
    val smallCardHeight = 100.dp
    val mediumCardHeight = 150.dp
    val largeCardHeight = 200.dp
    val largeCardPadding = Dimens.space12
    val smallCardPadding = Dimens.space8
    val mediumCardPadding = Dimens.space10
}
