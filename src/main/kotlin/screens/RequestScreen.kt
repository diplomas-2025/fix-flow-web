package screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import api.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.prefs.Preferences

data class RequestScreen(
    val id: Int,
    val user: UserEntityDto
): Screen {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var request by remember { mutableStateOf<RequestEntityDto?>(null) }
        val messages = remember { mutableStateListOf<CommentEntityDto>() }
        val preferences = remember { Preferences.userRoot().node("user") }

        LaunchedEffect(Unit) {
            request = RetrofitClient.instance.getRequestById(
                id = id,
                token = "Bearer " + preferences.get("token", null)
            )
            messages.clear()
            messages.addAll(RetrofitClient.instance.getAllCommands(
                id = id,
                token = "Bearer " + preferences.get("token", null)
            ).sortedBy { it.createdAt })
        }

        request?.let { it ->
            RequestDetailScreen(
                request = it,
                user = user,
                messages = messages,
                onSendMessage = {
                    scope.launch {
                        messages.add(
                            RetrofitClient.instance.createCommand(
                                id = id,
                                command = it,
                                token = "Bearer " + preferences.get("token", null)
                            )
                        )
                    }
                },
                onStatusChanged = {
                    scope.launch {
                        RetrofitClient.instance.updateStatus(
                            id = id,
                            status = it,
                            token = "Bearer " + preferences.get("token", null)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun RequestDetailScreen(
    request: RequestEntityDto,
    user: UserEntityDto,
    messages: List<CommentEntityDto>,
    onSendMessage: (String) -> Unit,
    onStatusChanged: (RequestStatus) -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow
    var newMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navigator.pop()
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Заявка: ${request.title}",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                )
            }
            if (user.role == UserRole.ItSupport) {
                ChangeStatusButton(
                    request = request,
                    onStatusChanged = onStatusChanged
                )
            }
        }

        Text("Описание: ${request.description}", style = TextStyle(fontSize = 16.sp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Статус: ${request.status.title}", style = TextStyle(fontSize = 16.sp))
        Spacer(modifier = Modifier.height(8.dp))

        // Даты
        Text("Дата: ${parseDate(request.createdAt)}", style = TextStyle(fontSize = 12.sp, color = Color.Gray))
        Spacer(modifier = Modifier.height(16.dp))

        // Список сообщений (чат)
        Text("Сообщения:", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                MessageCard(message)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        MessageInputField(
            newMessage = newMessage,
            onMessageChange = {
                newMessage = it
            },
            onSendMessage = {
                onSendMessage(newMessage)
            }
        )
    }
}

@Composable
fun MessageInputField(
    newMessage: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF8F9FA), shape = RoundedCornerShape(24.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newMessage,
            onValueChange = onMessageChange,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { onSendMessage() }
            ),
            textStyle = TextStyle(fontSize = 16.sp),
            placeholder = { Text("Введите сообщение...", color = Color.Gray) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = Color(0xFF1E88E5),
                focusedBorderColor = Color(0xFF1E88E5),
                unfocusedBorderColor = Color.LightGray
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        IconButton(
            onClick = onSendMessage,
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .background(Color(0xFF1E88E5), shape = CircleShape)
                .clickable { onSendMessage() }
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Отправить",
                tint = Color.White
            )
        }
    }
}



@Composable
fun MessageCard(message: CommentEntityDto) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message.user.username + " (${message.user.role.title})",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.comment,
                style = TextStyle(fontSize = 14.sp, color = Color.Black)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = parseDate(message.createdAt),
                    style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                )
            }
        }
    }
}

@Composable
fun ChangeStatusButton(request: RequestEntityDto, onStatusChanged: (RequestStatus) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(request.status) }

    // Кнопка для открытия диалога изменения статуса
    Button(
        onClick = { showDialog = true },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
    ) {
        Text(text = "Изменить статус", color = Color.White)
    }

    // Диалог выбора статуса
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {},
            text = {
                Column {
                    Text("Выберите новый статус", style = MaterialTheme.typography.h6)

                    Spacer(Modifier.height(5.dp))

                    RequestStatus.entries.forEach { status ->
                        // Создаем карточки для статусов
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp)
                                .clickable {
                                    selectedStatus = status
                                    onStatusChanged(status)  // Передаем выбранный статус
                                    showDialog = false  // Закрываем диалог
                                },
                            shape = MaterialTheme.shapes.medium,
                            elevation = 4.dp,
                            border = BorderStroke(
                                if (status == selectedStatus) 3.dp else 0.dp,
                                MaterialTheme.colors.primary
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Можно добавить иконки для статусов
                                Icon(
                                    imageVector = when (status) {
                                        RequestStatus.Open -> Icons.Default.Info
                                        RequestStatus.InProgress -> Icons.Default.Build
                                        RequestStatus.Closed -> Icons.Default.Check
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = status.title,
                                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Можно добавить логику для подтверждения изменений
                        showDialog = false
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Отменить")
                }
            }
        )
    }
}

fun parseDate(dateString: String): String {
    val instant = Instant.parse(dateString) // Преобразуем в Instant
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss") // Формат вывода
        .withZone(ZoneId.systemDefault()) // Указываем временную зону
    return formatter.format(instant) // Форматируем дату
}