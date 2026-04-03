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

private val ObsidianPlum = Color(0xFF71627A)
private val ObsidianMulberry = Color(0xFF5B4965)
private val ObsidianShadow = Color(0xFF3D354B)
private val ObsidianCore = Color(0xFF2E293A)

// PaySmart variant — light
val PaySmartLightColorTokens = DSColorTokens(
    brandPrimary = Color(0xFF256B49),
    brandSecondary = Color(0xFF274D61),
    brandAccent = Color(0xFF4D9B74),
    backgroundPrimary = Color(0xFFEEF0F2),
    backgroundSecondary = Color(0xFFEDE7DC),
    backgroundGradientTop = Color(0xFFFBF7F0),
    backgroundGradientMiddle = Color(0xFFF0F4EA),
    backgroundGradientBottom = Color(0xFFF8F2E8),
    surfacePrimary = Color(0xFFFFFCF7),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceInverse = Color(0xFF17201B),
    textPrimary = Color(0xFF17211C),
    textSecondary = Color(0xFF4D5A55),
    textTertiary = Color(0xFF73807A),
    textInverse = Color(0xFFF8F5EF),
    textDisabled = Color(0xFFB2B8B1),
    borderSubtle = Color(0xFFE5DDD0),
    borderStrong = Color(0xFF8B978F),
    divider = Color(0xFFE1D9CC),
    success = Color(0xFF256B49),
    warning = Color(0xFFF57C00),
    error = Color(0xFFB00020),
    info = Color(0xFF0288D1),
    fillActive = Color(0xFF256B49),
    fillHover = Color(0xFFE9F1E7),
    fillPressed = Color(0xFFDCE8D9),
    fillDisabled = Color(0xFFE8E2D8),
    buttonPrimaryBackground = Color(0xFF256B49),
    buttonPrimaryForeground = Color(0xFFFFFFFF),
    buttonSecondaryBackground = Color(0xFFE9EFE6),
    tabActiveBackground = Color(0xFFDDEBDD),
    tabInactiveBackground = Color.Transparent,
)

// PaySmart variant — dark
val PaySmartDarkColorTokens = DSColorTokens(
    brandPrimary = Color(0xFF4CAF82),
    brandSecondary = Color(0xFF8ED5B1),
    brandAccent = Color(0xFF80C9A0),
    backgroundPrimary = Color(0xFF0D1117),
    backgroundSecondary = Color(0xFF161B22),
    backgroundGradientTop = Color(0xFF0D1117),
    backgroundGradientMiddle = Color(0xFF1A2D20),
    backgroundGradientBottom = Color(0xFF0D1117),
    surfacePrimary = Color(0xFF161B22),
    surfaceElevated = Color(0xFF21262D),
    surfaceInverse = Color(0xFFF0F6FF),
    textPrimary = Color(0xFFF0F6FF),
    textSecondary = Color(0xFF8B949E),
    textTertiary = Color(0xFF6E7681),
    textInverse = Color(0xFF0D1117),
    textDisabled = Color(0xFF484F58),
    borderSubtle = Color(0xFF21262D),
    borderStrong = Color(0xFF8B949E),
    divider = Color(0xFF21262D),
    success = Color(0xFF3FB950),
    warning = Color(0xFFD29922),
    error = Color(0xFFF85149),
    info = Color(0xFF58A6FF),
    fillActive = Color(0xFF4CAF82),
    fillHover = Color(0xFF1A2D20),
    fillPressed = Color(0xFF1F3A28),
    fillDisabled = Color(0xFF21262D),
    buttonPrimaryBackground = Color(0xFF238636),
    buttonPrimaryForeground = Color(0xFFD2F4DC),
    buttonSecondaryBackground = Color(0xFF21262D),
    tabActiveBackground = Color(0xFF1A2D20),
    tabInactiveBackground = Color.Transparent,
)

// Obsidian variant — light
val ObsidianLightColorTokens = DSColorTokens(
    brandPrimary = ObsidianMulberry,
    brandSecondary = ObsidianShadow,
    brandAccent = ObsidianPlum,
    backgroundPrimary = Color(0xFFF6F4F8),
    backgroundSecondary = Color(0xFFEFEAF2),
    backgroundGradientTop = Color(0xFFFDFCFE),
    backgroundGradientMiddle = Color(0xFFF4F0F7),
    backgroundGradientBottom = Color(0xFFECE6F0),
    surfacePrimary = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFAF8FC),
    surfaceInverse = ObsidianCore,
    textPrimary = ObsidianCore,
    textSecondary = Color(0xFF5C5366),
    textTertiary = Color(0xFF7C7187),
    textInverse = Color(0xFFF8F4FA),
    textDisabled = Color(0xFFB5AEBE),
    borderSubtle = Color(0xFFE2DCE7),
    borderStrong = Color(0xFF87798F),
    divider = Color(0xFFDCD4E2),
    success = Color(0xFF3C7A52),
    warning = Color(0xFFB16C17),
    error = Color(0xFFB64256),
    info = Color(0xFF6C5C87),
    fillActive = ObsidianMulberry,
    fillHover = Color(0xFFF3EEF7),
    fillPressed = Color(0xFFE7E0ED),
    fillDisabled = Color(0xFFEDE7F1),
    buttonPrimaryBackground = ObsidianShadow,
    buttonPrimaryForeground = Color(0xFFFFF9FD),
    buttonSecondaryBackground = Color(0xFFEAE3EE),
    tabActiveBackground = Color(0xFFE5DDE8),
    tabInactiveBackground = Color.Transparent,
)

// Obsidian variant — dark
val ObsidianDarkColorTokens = DSColorTokens(
    brandPrimary = ObsidianPlum,
    brandSecondary = ObsidianMulberry,
    brandAccent = ObsidianShadow,
    backgroundPrimary = Color(0xFF14101A),
    backgroundSecondary = Color(0xFF1A1621),
    backgroundGradientTop = Color(0xFF17131D),
    backgroundGradientMiddle = Color(0xFF221C2A),
    backgroundGradientBottom = Color(0xFF110E16),
    surfacePrimary = Color(0xFF1B1622),
    surfaceElevated = Color(0xFF241E2D),
    surfaceInverse = Color(0xFFF6F2F9),
    textPrimary = Color(0xFFF6F2F9),
    textSecondary = Color(0xFFC8BECF),
    textTertiary = Color(0xFF9A8EA3),
    textInverse = ObsidianCore,
    textDisabled = Color(0xFF6C6174),
    borderSubtle = Color(0xFF312A3C),
    borderStrong = ObsidianPlum,
    divider = Color(0xFF3A3246),
    success = Color(0xFF66BB6A),
    warning = Color(0xFFE0A348),
    error = Color(0xFFEF6B73),
    info = Color(0xFF9C8BC0),
    fillActive = ObsidianPlum,
    fillHover = ObsidianCore,
    fillPressed = ObsidianShadow,
    fillDisabled = Color(0xFF241E2D),
    buttonPrimaryBackground = ObsidianPlum,
    buttonPrimaryForeground = Color(0xFFFCF8FF),
    buttonSecondaryBackground = ObsidianCore,
    tabActiveBackground = ObsidianShadow,
    tabInactiveBackground = Color.Transparent,
)

val LocalDSColorTokens = staticCompositionLocalOf { PaySmartLightColorTokens }
