import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import screens.AuthScreen
import java.util.prefs.Preferences

// Primary Color (deep blue shade)
val CustomPrimaryColor = Color(0xFF1E3A8A)  // Darker blue, vibrant and professional

// Secondary Color (soft teal)
val CustomSecondaryColor = Color(0xFF4FD1C5)  // Fresh teal, adds contrast and energy

// Background Color (light neutral)
val CustomBackgroundColor = Color(0xFFF0F4F8)  // Soft, light background to make content stand out

// Error Color (soft red for alerts)
val CustomErrorColor = Color(0xFFEF4444)  // Modern red for error messages, soft yet noticeable

// Optional Additional Colors

// Surface Color (white with a slight shade)
val CustomSurfaceColor = Color(0xFFFFFFFF)  // White for surfaces and cards

// On Surface Color (dark gray for text on white surfaces)
val CustomOnSurfaceColor = Color(0xFF333333)  // Dark gray for text and icons on surfaces

// On Primary Color (light text on primary background)
val CustomOnPrimaryColor = Color(0xFFFFFFFF)  // White text to contrast with primary color

// Accent Color (for highlights or call to actions)
val CustomAccentColor = Color(0xFF9B56FF)  // A light purple accent for highlights or focus areas


private val CustomColors = lightColors(
    primary = CustomPrimaryColor,
    secondary = CustomSecondaryColor,
    background = CustomBackgroundColor,
    surface = CustomSurfaceColor,
    error = CustomErrorColor,
    onPrimary = CustomOnPrimaryColor,
    onSecondary = CustomOnPrimaryColor,
    onBackground = CustomOnSurfaceColor,
    onSurface = CustomOnSurfaceColor,
    onError = Color.White
)

@Composable
@Preview
fun App() {
    val preferences = remember { Preferences.userRoot().node("user") }

    MaterialTheme(
        colors = CustomColors
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colors.background, Color(0xFFE0E0E0))))
        )

        Navigator(if (preferences.get("token", null) != null) AuthScreen() else AuthScreen())
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            size = DpSize(1200.dp, 800.dp)
        ),
        title = "ООО «ВАРНОФФ»"
    ) {
        App()
    }
}