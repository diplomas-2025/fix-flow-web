package screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

        fun onLoginNavigate() {
            navigator.push(AuthScreen())
        }

        // Градиентный фон для всей страницы
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A5ACD), Color(0xFF20B2AA))
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Круглый логотип/иконка в верхней части
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = Color(0xFFEDE7F6),
                    elevation = 8.dp
                ) {
                    Image(
                        painter = painterResource("authentication.png"),
                        contentDescription = "Logo",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Text(
                    text = "Создать аккаунт",
                    fontSize = 28.sp,
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF333333)
                    )
                )

                // Username TextField с иконкой
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Имя пользователя") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                // Email TextField с иконкой
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        autoCorrect = false,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )

                // Password TextField с иконкой и скрытием текста
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                errorMessage?.let {
                    Text(it, color = Color.Red, fontWeight = FontWeight.Medium)
                }

                AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                    CircularProgressIndicator()
                }

                Button(
                    onClick = { onSignUp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.elevation(8.dp),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && username.isNotBlank()
                ) {
                    Text("Зарегистрироваться", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { onLoginNavigate() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        "Уже есть аккаунт? Войти",
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
