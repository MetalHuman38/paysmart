package net.metalbrain.paysmart.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
object HomeCardTokens {

    const val OUTLINE_ALPHA = 0.72f
    const val SURFACE_OVERLAY_ALPHA = 0.82f
    val summaryCardHeight = 186.dp
    val accountInfoCardWidth = 312.dp
    val accountInfoCardHeight = 218.dp
    val serviceCardWidth = 116.dp
    val serviceCardHeight = 132.dp
    val serviceCardIconPadding = Dimens.md
    val contentPadding = Dimens.md
    val compactContentPadding = Dimens.md
    val cardShape = RoundedCornerShape(24.dp)
    val serviceCardShape = RoundedCornerShape(24.dp)
    val defaultElevation = 2.dp
    val subtleElevation = 1.dp
}
