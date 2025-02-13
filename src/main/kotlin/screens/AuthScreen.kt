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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import api.RetrofitClient
import api.SignInParams
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import java.util.prefs.Preferences

class AuthScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val preferences = remember { Preferences.userRoot().node("user") }

        fun onLogin() {
            scope.launch {
                isLoading = true
                try {
                    val response = RetrofitClient.instance.signIn(SignInParams(email, password))
                    if (response.isSuccessful) {
                        preferences.put("token", response.body()!!.accessToken)
                        navigator.push(MainScreen())
                    } else {
                        errorMessage = "Ошибка входа. Пожалуйста, проверьте данные."
                    }
                } catch (e: Exception) {
                    errorMessage = "Ошибка подключения. Пожалуйста, попробуйте позже."
                }
                isLoading = false
            }
        }

        fun onRegNavigate() {
            navigator.push(SignUpScreen())
        }

        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource("authentication.png"),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp).padding(bottom = 16.dp)
            )

            Text("Вход", fontSize = 24.sp, color = MaterialTheme.colors.primary)
            Spacer(modifier = Modifier.height(16.dp))

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
                Button(
                    onClick = { onLogin() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6A5ACD), // Цвет фона кнопки (сиреневый)
                        contentColor = Color.White // Цвет текста
                    ),
                    elevation = ButtonDefaults.elevation(8.dp)
                ) {
                    Text("Войти")
                }

                Button(
                    onClick = { onRegNavigate() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF20B2AA), // Цвет фона кнопки (светлый бирюзовый)
                        contentColor = Color.White // Цвет текста
                    ),
                    elevation = ButtonDefaults.elevation(8.dp)
                ) {
                    Text("Зарегистрироваться")
                }
            }


        }
    }
}
