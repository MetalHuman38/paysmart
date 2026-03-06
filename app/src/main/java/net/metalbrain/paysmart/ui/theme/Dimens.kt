package net.metalbrain.paysmart.ui.theme

import androidx.compose.ui.unit.dp

object Dimens {
    // 8pt spacing rhythm
    val space2 = 4.dp
    val space4 = 8.dp
    val space6 = 12.dp
    val space8 = 16.dp
    val space10 = 20.dp
    val space12 = 24.dp
    val space16 = 32.dp

    // Screen paddings
    val screenPadding = space12
    val smallScreenPadding = space4
    val mediumScreenPadding = space8
    val largeScreenPadding = space16

    // Legacy aliases kept for compatibility
    val smallSpacing = space4
    val mediumSpacing = space8
    val largeSpacing = space16

    // Shared controls
    val buttonHeight = 52.dp
    val cornerRadius = 14.dp
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
