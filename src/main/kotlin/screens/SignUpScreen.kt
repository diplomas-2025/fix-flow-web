package screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.RetrofitClient
import api.SignUpParams
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import java.util.prefs.Preferences

class SignUpScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val scope = rememberCoroutineScope()
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val preferences = remember { Preferences.userRoot().node("user") }

        fun onSignUp() {
            scope.launch {
                isLoading = true
                try {
                    val response = RetrofitClient.instance.signUp(SignUpParams(username, email, password))
                    if (response.isSuccessful) {
                        preferences.put("token", response.body()!!.accessToken)
                        navigator.push(MainScreen())
                    } else {
                        errorMessage = "Ошибка регистрации. Пожалуйста, попробуйте позже."
                    }
                } catch (e: Exception) {
                    errorMessage = "Ошибка подключения. Пожалуйста, попробуйте позже."
                }
                isLoading = false
            }
        }

        // Button click to navigate to the login screen
        fun onLoginNavigate() {
            navigator.push(AuthScreen()) // Navigate to the login screen
        }

        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Add logo
            Image(
                painter = painterResource("authentication.png"), // Replace with actual logo resource
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp).padding(bottom = 16.dp)
            )

            Text("Регистрация", fontSize = 24.sp, color = MaterialTheme.colors.primary)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Display error message if there is one
            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            }

            AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                CircularProgressIndicator()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка "Зарегистрироваться"
                Button(
                    onClick = { onSignUp() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6A5ACD), // Сиреневый цвет
                        contentColor = Color.White // Белый цвет текста
                    ),
                    elevation = ButtonDefaults.elevation(8.dp)
                ) {
                    Text("Зарегистрироваться")
                }

                // Кнопка "Уже есть аккаунт? Войти"
                Button(
                    onClick = { onLoginNavigate() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF20B2AA), // Бирюзовый цвет
                        contentColor = Color.White // Белый цвет текста
                    ),
                    elevation = ButtonDefaults.elevation(8.dp)
                ) {
                    Text("Уже есть аккаунт? Войти")
                }
            }
        }
    }
}
