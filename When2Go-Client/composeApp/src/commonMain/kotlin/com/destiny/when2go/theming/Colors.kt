import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Light Theme Colors
val LightRedPrimary = Color(0xffb52d2d)
val LightBlueSecondary = Color(0xffdbe9ff)
val LightBackground = Color(0xFFF8F8F8)
val LightTextColor = Color(0xFF333333)


// Dark Theme Colors
val DarkRedPrimary = Color(0xff872020)
val DarkBlueSecondary = Color(0xff162947)
val DarkBackground = Color(0xFF1C1C1C)
val DarkTextColor = Color(0xFFE0E0E0)

internal val LightColorScheme = lightColorScheme(
    primary = LightRedPrimary,
    onPrimary = DarkTextColor,
    secondary = LightBlueSecondary,
    onSecondary = DarkBlueSecondary,
    secondaryContainer = LightBlueSecondary,
    onSecondaryContainer = DarkBlueSecondary,
    background = LightBackground,
    onBackground = LightTextColor,
    surface = LightBackground,
    onSurface = LightTextColor,
)

internal val DarkColorScheme = darkColorScheme(
    primary = DarkRedPrimary,
    onPrimary = DarkTextColor,
    secondary = DarkBlueSecondary,
    onSecondary = LightBlueSecondary,
    secondaryContainer = DarkBlueSecondary,
    onSecondaryContainer = LightBlueSecondary,
    background = DarkBackground,
    onBackground = DarkTextColor,
    surface = DarkBackground,
    onSurface = DarkTextColor,
)


val green: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xff72e88f) else Color(0xff409c57)

val tableRowBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xff333333) else Color(0xffdedede)

val tableRowHighlightedBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xff666464) else Color(0xfffafafa)

val tableBorderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xff474747) else Color(0xffcccccc)