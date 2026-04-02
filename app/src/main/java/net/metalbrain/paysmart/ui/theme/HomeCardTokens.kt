package net.metalbrain.paysmart.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
object HomeCardTokens {

    const val OUTLINE_ALPHA = 0.72f
    const val SURFACE_OVERLAY_ALPHA = 0.82f
    val summaryCardHeight = 168.dp
    val accountInfoCardWidth = 296.dp
    val accountInfoCardHeight = 176.dp
    val recentRecipientCardWidth = 148.dp
    val recentRecipientCardHeight = 164.dp
    val recentRecipientAvatarSize = 64.dp
    val serviceCardWidth = 92.dp
    val serviceCardHeight = 112.dp
    val serviceCircleSize = 74.dp
    val serviceCardIconPadding = Dimens.md
    val contentPadding = 14.dp
    val compactContentPadding = 14.dp
    val cardShape = RoundedCornerShape(20.dp)
    val serviceCardShape = RoundedCornerShape(22.dp)
    val defaultElevation = 1.dp
    val subtleElevation = 0.5.dp
}
