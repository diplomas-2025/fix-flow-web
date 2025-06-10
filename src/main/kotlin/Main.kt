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

val CustomPrimaryColor = Color(0xFF2E7D32)     // Тёмно-зелёный
val CustomSecondaryColor = Color(0xFF8BC34A)   // Светло-зелёный
val CustomBackgroundColor = Color(0xFFF1F8E9)  // Очень светлый зелёный фон
val CustomErrorColor = Color(0xFFC62828)       // Красный
val CustomSurfaceColor = Color(0xFFFFFFFF)     // Белый
val CustomOnSurfaceColor = Color(0xFF212121)   // Тёмно-серый текст
val CustomOnPrimaryColor = Color(0xFFFFFFFF)   // Белый текст
val CustomAccentColor = Color(0xFFFFC107)      // Жёлтый акцент

val CustomColors = lightColors(
    primary = CustomPrimaryColor,
    secondary = CustomSecondaryColor,
    background = CustomBackgroundColor,
    surface = CustomSurfaceColor,
    error = CustomErrorColor,
    onPrimary = CustomOnPrimaryColor,
    onSecondary = Color.Black,
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
        title = "ООО «ПромТехСервис»"
    ) {
        App()
    }
}