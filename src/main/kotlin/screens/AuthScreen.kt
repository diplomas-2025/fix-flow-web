package screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        var passwordVisible by remember { mutableStateOf(false) }

        val preferences = remember { Preferences.userRoot().node("user") }

        fun onLogin() {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val response = RetrofitClient.instance.signIn(SignInParams(email, password))
                    if (response.isSuccessful) {
                        preferences.put("token", response.body()!!.accessToken)
                        navigator.push(MainScreen())
                    } else {
                        errorMessage = "Неверный email или пароль"
                    }
                } catch (e: Exception) {
                    errorMessage = "Ошибка подключения. Попробуйте позже."
                }
                isLoading = false
            }
        }

        fun onRegNavigate() {
            navigator.push(SignUpScreen())
        }

        // Фон с градиентом
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A5ACD), Color(0xFF20B2AA))
                    )
                )
        ) {
            // Центрированный контейнер с белым фоном и тенями
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.Center)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                elevation = 12.dp,
                backgroundColor = Color.White.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Добро пожаловать",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Войдите в аккаунт, чтобы продолжить",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Пароль
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Пароль") },
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onLogin() }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(32.dp),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text(
                            text = "Войти",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { onRegNavigate() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(
                            text = "Нет аккаунта? Зарегистрироваться",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
