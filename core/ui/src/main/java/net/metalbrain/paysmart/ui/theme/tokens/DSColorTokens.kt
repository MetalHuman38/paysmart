package net.metalbrain.paysmart.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class DSColorTokens(
    // Brand
    val brandPrimary: Color,
    val brandSecondary: Color,
    val brandAccent: Color,
    // Background / Surface
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundGradientTop: Color,
    val backgroundGradientMiddle: Color,
    val backgroundGradientBottom: Color,
    val surfacePrimary: Color,
    val surfaceElevated: Color,
    val surfaceInverse: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textInverse: Color,
    val textDisabled: Color,
    // Border / Divider
    val borderSubtle: Color,
    val borderStrong: Color,
    val divider: Color,
    // Semantic
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    // Interaction
    val fillActive: Color,
    val fillHover: Color,
    val fillPressed: Color,
    val fillDisabled: Color,
    // Component semantic
    val buttonPrimaryBackground: Color,
    val buttonPrimaryForeground: Color,
    val buttonSecondaryBackground: Color,
    val tabActiveBackground: Color,
    val tabInactiveBackground: Color,
)

val PaySmartLightColorTokens = DSColorTokens(
    brandPrimary = Color(0xFF116149),
    brandSecondary = Color(0xFF255B73),
    brandAccent = Color(0xFFC26A2C),
    backgroundPrimary = Color(0xFFF6F8FA),
    backgroundSecondary = Color(0xFFEFF3F6),
    backgroundGradientTop = Color(0xFFF6F8FA),
    backgroundGradientMiddle = Color(0xFFF6F8FA),
    backgroundGradientBottom = Color(0xFFF6F8FA),
    surfacePrimary = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF0F4F7),
    surfaceInverse = Color(0xFF070A0F),
    textPrimary = Color(0xFF111820),
    textSecondary = Color(0xFF52616D),
    textTertiary = Color(0xFF7B8792),
    textInverse = Color(0xFFFFFFFF),
    textDisabled = Color(0xFFABB4BC),
    borderSubtle = Color(0xFFDDE4EA),
    borderStrong = Color(0xFF8997A3),
    divider = Color(0xFFE4E9EE),
    success = Color(0xFF157A4E),
    warning = Color(0xFFB86B00),
    error = Color(0xFFB3261E),
    info = Color(0xFF1F6E8C),
    fillActive = Color(0xFF116149),
    fillHover = Color(0xFFE6F2EE),
    fillPressed = Color(0xFFD8E9E3),
    fillDisabled = Color(0xFFE6EBEF),
    buttonPrimaryBackground = Color(0xFF116149),
    buttonPrimaryForeground = Color(0xFFFFFFFF),
    buttonSecondaryBackground = Color(0xFFE7EEF2),
    tabActiveBackground = Color(0xFFDDEBE6),
    tabInactiveBackground = Color.Transparent,
)

val PaySmartDarkColorTokens = DSColorTokens(
    brandPrimary = Color(0xFF48D597),
    brandSecondary = Color(0xFF7AC7D6),
    brandAccent = Color(0xFFF0B35B),
    backgroundPrimary = Color(0xFF070A0F),
    backgroundSecondary = Color(0xFF0C1118),
    backgroundGradientTop = Color(0xFF070A0F),
    backgroundGradientMiddle = Color(0xFF070A0F),
    backgroundGradientBottom = Color(0xFF070A0F),
    surfacePrimary = Color(0xFF10161D),
    surfaceElevated = Color(0xFF17202A),
    surfaceInverse = Color(0xFFF7FAFC),
    textPrimary = Color(0xFFF3F7FA),
    textSecondary = Color(0xFFA8B3BD),
    textTertiary = Color(0xFF7D8994),
    textInverse = Color(0xFF07100C),
    textDisabled = Color(0xFF596571),
    borderSubtle = Color(0xFF202B36),
    borderStrong = Color(0xFF667482),
    divider = Color(0xFF1C2630),
    success = Color(0xFF48D597),
    warning = Color(0xFFF0B35B),
    error = Color(0xFFFF6B64),
    info = Color(0xFF7AC7D6),
    fillActive = Color(0xFF48D597),
    fillHover = Color(0xFF12231D),
    fillPressed = Color(0xFF183429),
    fillDisabled = Color(0xFF202832),
    buttonPrimaryBackground = Color(0xFF1F9D64),
    buttonPrimaryForeground = Color(0xFFFFFFFF),
    buttonSecondaryBackground = Color(0xFF17202A),
    tabActiveBackground = Color(0xFF12231D),
    tabInactiveBackground = Color.Transparent,
)

val LocalDSColorTokens = staticCompositionLocalOf { PaySmartLightColorTokens }
