package screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.RetrofitClient
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import java.util.prefs.Preferences

class CreateRequestScreen : Screen {
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val preferences = remember { Preferences.userRoot().node("user") }
        val navigator = LocalNavigator.currentOrThrow

        fun onCreateRequest() {
            scope.launch {
                isLoading = true
                try {
                    val response = RetrofitClient.instance.createRequest(
                        title = title,
                        desc = description,
                        token = "Bearer " + preferences.get("token", null)
                    )
                    if (response.isSuccessful) {
                        successMessage = "Заявка успешно создана!"
                        title = ""
                        description = ""
                        navigator.pop()
                    } else {
                        errorMessage = "Ошибка при создании заявки. Попробуйте снова."
                    }
                } catch (e: Exception) {
                    errorMessage = "Ошибка подключения. Пожалуйста, попробуйте позже."
                }
                isLoading = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Кнопка "Назад"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }

            Image(
                painter = painterResource("ask-question.png"),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp).padding(bottom = 5.dp, top = 16.dp)
            )

            // Заголовок экрана
            Text("Создать заявку", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

            // Поле для ввода заголовка заявки
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок заявки") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Поле для ввода описания заявки
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание заявки") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Показываем сообщения об ошибке или успехе
            successMessage?.let {
                Text(it, color = MaterialTheme.colors.primary, modifier = Modifier.padding(bottom = 8.dp))
            }
            errorMessage?.let {
                Text(it, color = MaterialTheme.colors.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Индикатор загрузки
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }

            // Кнопка для отправки заявки
            Button(
                onClick = { onCreateRequest() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Создать заявку")
            }
        }
    }
}
