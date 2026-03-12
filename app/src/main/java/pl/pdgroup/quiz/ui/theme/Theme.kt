package pl.pdgroup.quiz.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = SurfaceLight,
    primaryContainer = OrangePrimaryDarkVariantDark,
    secondary = OrangeSecondaryDark,
    secondaryContainer = YellowSecondaryLightVariantDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimaryLight,
    onPrimary = SurfaceLight,
    primaryContainer = OrangePrimaryDarkVariant,
    secondary = OrangeSecondaryLight,
    secondaryContainer = YellowSecondaryLightVariant,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorLight
)

@Composable
fun QuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to enforce custom theme
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            if (highContrast) DarkColorScheme.copy(
                primary = HighContrastPrimaryDark,
                error = ErrorDark.copy(red = 1f) // Just an example of high contrast saturation
            ) else DarkColorScheme
        }
        else -> {
            if (highContrast) LightColorScheme.copy(
                primary = HighContrastPrimaryLight,
                error = ErrorLight.copy(red = 0.8f) // saturated
            ) else LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}