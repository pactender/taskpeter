package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeStyle(val displayName: String, val subtitle: String, val isDark: Boolean) {
    RADIANT_CRIMSON("Radiant Crimson", "High-contrast obsidian with crimson red & electric cyan", true),
    NEON_PUNK("Neon Punk", "Deep violet with neon magenta, electric cyan & canary accents", true),
    SOLAR_HORIZON("Solar Horizon", "Dark slate with hyper orange & pulse cobalt", true),
    TASKPETER_PROTOCOL("Protocol Neo-Noir", "Cinematic cyber-tactical cobalt with laser cyan & neon orange", true),
    TASKPETER_OBSIDIAN("Obsidian Mint", "Cockpit void with mint telemetry & precision cyan", true),
    TASKPETER_MATRIX("Terminal Matrix", "Deep terminal with electric emerald & amber", true),
    TASKPETER_SOLAR("Solar Flux", "Telemetry obsidian with warm amber & terracotta", true),
    TASKPETER_ARCTIC("Arctic Navy", "Sub-zero cobalt with frosty glacier cyan", true),
    TASKPETER_PAPER("Daylight Studio", "Architectural paper with high-contrast obsidian ink", false);

    companion object {
        val VALORANT_RADIANT: AppThemeStyle get() = RADIANT_CRIMSON
        val SPIDER_VERSE: AppThemeStyle get() = NEON_PUNK
        val OVERWATCH_HUD: AppThemeStyle get() = SOLAR_HORIZON
    }
}

// 0. Radiant Crimson Scheme (High-contrast tactical dark theme with crimson red & electric cyan)
val RadiantCrimsonColorScheme = darkColorScheme(
    primary = RadiantRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1016),
    onPrimaryContainer = Color(0xFFFFD5D8),
    secondary = RadiantCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003830),
    onSecondaryContainer = Color(0xFF99FCEB),
    tertiary = RadiantYellow,
    onTertiary = Color.Black,
    background = RadiantBg,
    onBackground = RadiantChalk,
    surface = RadiantSurface,
    onSurface = RadiantChalk,
    surfaceVariant = RadiantElevated,
    onSurfaceVariant = RadiantMuted,
    outline = RadiantBorder,
    error = RadiantRed,
    onError = Color.White
)

val ValorantRadiantColorScheme = RadiantCrimsonColorScheme

// 1. Neon Punk Scheme (Deep violet with electric pink, cyan & canary pop)
val NeonPunkColorScheme = darkColorScheme(
    primary = NeonPunkPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C0026),
    onPrimaryContainer = Color(0xFFFFD1E5),
    secondary = NeonPunkCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003C40),
    onSecondaryContainer = Color(0xFFB5FCFF),
    tertiary = NeonPunkYellow,
    onTertiary = Color.Black,
    background = NeonPunkBg,
    onBackground = NeonPunkText,
    surface = NeonPunkSurface,
    onSurface = NeonPunkText,
    surfaceVariant = NeonPunkElevated,
    onSurfaceVariant = Color(0xFFC4B5FD),
    outline = NeonPunkBorder,
    error = NeonPunkPink,
    onError = Color.White
)

val SpiderVerseColorScheme = NeonPunkColorScheme

// 2. Solar Horizon Scheme (Sci-fi dark slate with hyper orange & shield cobalt)
val SolarHorizonColorScheme = darkColorScheme(
    primary = HorizonOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF472200),
    onPrimaryContainer = Color(0xFFFFDEC2),
    secondary = HorizonBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF002B47),
    onSecondaryContainer = Color(0xFFB5E4FF),
    tertiary = Color(0xFFFF5252),
    onTertiary = Color.White,
    background = HorizonBg,
    onBackground = HorizonWhite,
    surface = HorizonSurface,
    onSurface = HorizonWhite,
    surfaceVariant = HorizonElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = HorizonBorder,
    error = Color(0xFFFF334B),
    onError = Color.White
)

val OverwatchHeroColorScheme = SolarHorizonColorScheme

// 3. TaskPeter Protocol Neo-Noir Scheme
val TaskPeterProtocolColorScheme = darkColorScheme(
    primary = ProtocolNeonOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1800),
    onPrimaryContainer = Color(0xFFFFD5B8),
    secondary = ProtocolNeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003847),
    onSecondaryContainer = Color(0xFFB5F4FF),
    tertiary = ProtocolCobaltBlue,
    onTertiary = Color.White,
    background = ProtocolBg,
    onBackground = ProtocolTextHigh,
    surface = ProtocolSurface,
    onSurface = ProtocolTextHigh,
    surfaceVariant = ProtocolElevated,
    onSurfaceVariant = ProtocolTextMedium,
    outline = ProtocolBorder,
    error = DevRose,
    onError = Color.White
)

// 4. TaskPeter Obsidian Scheme
val TaskPeterObsidianColorScheme = darkColorScheme(
    primary = JetBrainsMint,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF163E2B),
    onPrimaryContainer = Color(0xFF98F5BA),
    secondary = JetBrainsBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1B3260),
    onSecondaryContainer = Color(0xFFBED5FF),
    tertiary = DevPurple,
    onTertiary = Color.White,
    background = JetBrainsBg,
    onBackground = JetBrainsTextHigh,
    surface = JetBrainsSurface,
    onSurface = JetBrainsTextHigh,
    surfaceVariant = JetBrainsElevated,
    onSurfaceVariant = JetBrainsTextMedium,
    outline = JetBrainsBorder,
    error = DevRose,
    onError = Color.White
)

// 5. TaskPeter Matrix Scheme
val TaskPeterMatrixColorScheme = darkColorScheme(
    primary = DevEmerald,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF06331A),
    onPrimaryContainer = Color(0xFF86EFAC),
    secondary = DevAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF452205),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = DevCyan,
    onTertiary = Color.Black,
    background = Color(0xFF080C0A),
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF0F1713),
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF19251E),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF234231),
    error = DevRose,
    onError = Color.White
)

// 6. TaskPeter Solar Flux Scheme
val TaskPeterSolarColorScheme = darkColorScheme(
    primary = ClaudeWarmTerracotta,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF422108),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = DevAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF4A3419),
    onSecondaryContainer = Color(0xFFFFD59E),
    tertiary = ClaudeWarmSand,
    onTertiary = Color.Black,
    background = ClaudeDarkBg,
    onBackground = ClaudeDarkText,
    surface = ClaudeDarkSurface,
    onSurface = ClaudeDarkText,
    surfaceVariant = ClaudeDarkElevated,
    onSurfaceVariant = ClaudeDarkTextMuted,
    outline = ClaudeDarkBorder,
    error = DevRose,
    onError = Color.White
)

// 7. TaskPeter Arctic Navy Scheme
val TaskPeterArcticColorScheme = darkColorScheme(
    primary = DevCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0A2B42),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFF818CF8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E1B4B),
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = JetBrainsMint,
    onTertiary = Color.Black,
    background = Color(0xFF0B1320),
    onBackground = Color(0xFFF0F6FC),
    surface = Color(0xFF111D30),
    onSurface = Color(0xFFF0F6FC),
    surfaceVariant = Color(0xFF1A2B44),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF243B5E),
    error = DevRose,
    onError = Color.White
)

// 8. TaskPeter Daylight Studio
val TaskPeterPaperColorScheme = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = Color(0xFF78350F),
    onTertiary = Color.White,
    background = ClaudeLightBg,
    onBackground = ClaudeLightText,
    surface = ClaudeLightSurface,
    onSurface = ClaudeLightText,
    surfaceVariant = ClaudeLightElevated,
    onSurfaceVariant = ClaudeLightTextMuted,
    outline = ClaudeLightBorder,
    error = DevRose,
    onError = Color.White
)

val DeveloperDarkColorScheme = RadiantCrimsonColorScheme
val DeveloperLightColorScheme = TaskPeterPaperColorScheme

fun getColorSchemeForTheme(themeStyle: AppThemeStyle): ColorScheme {
    return when (themeStyle) {
        AppThemeStyle.RADIANT_CRIMSON -> RadiantCrimsonColorScheme
        AppThemeStyle.NEON_PUNK -> NeonPunkColorScheme
        AppThemeStyle.SOLAR_HORIZON -> SolarHorizonColorScheme
        AppThemeStyle.TASKPETER_PROTOCOL -> TaskPeterProtocolColorScheme
        AppThemeStyle.TASKPETER_OBSIDIAN -> TaskPeterObsidianColorScheme
        AppThemeStyle.TASKPETER_MATRIX -> TaskPeterMatrixColorScheme
        AppThemeStyle.TASKPETER_SOLAR -> TaskPeterSolarColorScheme
        AppThemeStyle.TASKPETER_ARCTIC -> TaskPeterArcticColorScheme
        AppThemeStyle.TASKPETER_PAPER -> TaskPeterPaperColorScheme
    }
}

val AppThemeStyle.accentColor: Color
    get() = when (this) {
        AppThemeStyle.RADIANT_CRIMSON -> RadiantRed
        AppThemeStyle.NEON_PUNK -> NeonPunkPink
        AppThemeStyle.SOLAR_HORIZON -> HorizonOrange
        AppThemeStyle.TASKPETER_PROTOCOL -> ProtocolNeonOrange
        AppThemeStyle.TASKPETER_OBSIDIAN -> JetBrainsMint
        AppThemeStyle.TASKPETER_MATRIX -> DevEmerald
        AppThemeStyle.TASKPETER_SOLAR -> ClaudeWarmTerracotta
        AppThemeStyle.TASKPETER_ARCTIC -> DevCyan
        AppThemeStyle.TASKPETER_PAPER -> Color(0xFF0F766E)
    }

val AppThemeStyle.secondaryAccentColor: Color
    get() = when (this) {
        AppThemeStyle.RADIANT_CRIMSON -> RadiantCyan
        AppThemeStyle.NEON_PUNK -> NeonPunkCyan
        AppThemeStyle.SOLAR_HORIZON -> HorizonBlue
        AppThemeStyle.TASKPETER_PROTOCOL -> ProtocolNeonCyan
        AppThemeStyle.TASKPETER_OBSIDIAN -> JetBrainsBlue
        AppThemeStyle.TASKPETER_MATRIX -> DevAmber
        AppThemeStyle.TASKPETER_SOLAR -> DevCyan
        AppThemeStyle.TASKPETER_ARCTIC -> Color(0xFF818CF8)
        AppThemeStyle.TASKPETER_PAPER -> Color(0xFF0284C7)
    }

@Composable
fun TaskPeterTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.RADIANT_CRIMSON,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = getColorSchemeForTheme(themeStyle)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
