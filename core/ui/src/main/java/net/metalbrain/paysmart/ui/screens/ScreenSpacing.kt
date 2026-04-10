package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens

object ScreenSpacing {

    val contentHorizontal: Dp = Dimens.screenPadding
    val contentVertical: Dp = Dimens.largeSpacing
    val sectionGap: Dp = Dimens.mediumSpacing
    val topBarHorizontal: Dp = Dimens.screenPadding
    val topBarTop: Dp = Dimens.mediumSpacing
    val topBarBottom: Dp = Dimens.largeSpacing
    val topBarMinHeight: Dp = 44.dp

    @Composable
    fun defaultContentPadding(): PaddingValues {
        return PaddingValues(
            start = contentHorizontal,
            top = contentVertical,
            end = contentHorizontal,
            bottom = contentVertical
        )
    }
}

internal fun Modifier.paddingSafe(
    paddingValues: PaddingValues
): Modifier {
    return this.then(
        Modifier.padding(
            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
            top = paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            bottom = paddingValues.calculateBottomPadding()
        )
    )
}
