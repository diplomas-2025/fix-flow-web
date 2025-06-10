package screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

                        request = RetrofitClient.instance.getRequestById(
                            id = id,
                            token = "Bearer " + preferences.get("token", null)
                        )
                    }
                },
                onRequestRefresh = {
                    scope.launch {
                        request = RetrofitClient.instance.getRequestById(
                            id = id,
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
    onStatusChanged: (RequestStatus) -> Unit,
    onRequestRefresh: () -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    var newMessage by remember { mutableStateOf("") }
    var feedbackDialog by remember { mutableStateOf(false) }

    val preferences = remember { Preferences.userRoot().node("user") }

    if (feedbackDialog) {
        FeedbackDialog(
            onDismiss = { feedbackDialog = false },
            onSubmit = { rating, text ->
                scope.launch {
                    RetrofitClient.instance.feedback(
                        id = request.id,
                        text = text,
                        rating = rating,
                        token = "Bearer " + preferences.get("token", null)
                    )

                    onRequestRefresh()
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { navigator.pop() }
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colors.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Заявка: ${request.title}",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (user.role == UserRole.ItSupport) {
                ChangeStatusButton(
                    request = request,
                    onStatusChanged = onStatusChanged
                )
            }
        }

        // Основная карточка с информацией
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = 4.dp,
            contentColor = MaterialTheme.colors.surface,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Блок с основной информацией
                InfoItem(
                    title = "Описание",
                    value = request.description,
                    icon = Icons.Filled.Edit
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )

                // Блок со статусом и категорией
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoItem(
                            title = "Статус",
                            value = request.status.title,
                            icon = Icons.Filled.Info
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        InfoItem(
                            title = "Категория",
                            value = request.category.name,
                            icon = Icons.Filled.Edit
                        )
                    }
                }

                // Обратная связь (если есть)
                request.satisfactionRating?.let {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                    InfoItem(
                        title = "Рейтинг",
                        value = "$it/5",
                        icon = Icons.Filled.Star,
                        iconTint = Color(0xFFFFC107)
                    )
                }

                request.feedbackText?.let {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                    InfoItem(
                        title = "Обратная связь",
                        value = it,
                        icon = Icons.Filled.Edit
                    )
                }

                // Дата внизу карточки
                Text(
                    text = "Создано: ${parseDate(request.createdAt)}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (request.satisfactionRating == null && request.status == RequestStatus.Closed && user.role == UserRole.Employee) {
            Button(
                onClick = {
                    feedbackDialog = !feedbackDialog
                }
            ) {
                Text(text = "Оставить обратную связь")
            }

            Spacer(Modifier.height(8.dp))
        }

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
                newMessage = ""
            }
        )
    }
}

@Composable
fun FeedbackDialog(
    initialRating: Int = 0,
    initialText: String = "",
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, feedback: String) -> Unit
) {
    var rating by remember { mutableStateOf(initialRating) }
    var feedbackText by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Оставьте обратную связь", style = MaterialTheme.typography.h6)
        },
        text = {
            Column {
                Text(text = "Оцените от 1 до 5", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    // Рейтинг: 5 иконок звезд
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Рейтинг $i",
                            tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { rating = i }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = { Text("Введите комментарий...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rating > 0) {
                        onSubmit(rating, feedbackText)
                        onDismiss()
                    }
                },
                enabled = rating > 0 // Не даём отправить без оценки
            ) {
                Text("Отправить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}


@Composable
fun InfoItem(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colors.primary
) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

@Composable
fun MessageInputField(
    newMessage: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(30.dp),
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = onMessageChange,
                placeholder = { Text("Введите сообщение...", color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)) },
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onSendMessage() }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    textColor = MaterialTheme.colors.onSurface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .background(MaterialTheme.colors.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Отправить",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}



@Composable
fun MessageCard(message: CommentEntityDto) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = message.user.username,
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = message.user.role.title,
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.primaryVariant)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message.comment,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = parseDate(message.createdAt),
                    style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                )
            }
        }
    }
}

@Composable
fun ChangeStatusButton(request: RequestEntityDto, onStatusChanged: (RequestStatus) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(request.status) }

    Button(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
        elevation = ButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(0.6f)
    ) {
        Text(
            text = "Изменить статус",
            color = Color.White,
            style = MaterialTheme.typography.button
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = {
                Column {
                    Text(
                        text = "Выберите новый статус",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    RequestStatus.entries.forEach { status ->
                        val isSelected = status == selectedStatus
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colors.surface)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colors.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedStatus = status
                                }
                        )  {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (status) {
                                    RequestStatus.Open -> Icons.Default.Info
                                    RequestStatus.InProgress -> Icons.Default.Build
                                    RequestStatus.Closed -> Icons.Default.Check
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = status.title,
                                    style = MaterialTheme.typography.subtitle1.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStatusChanged(selectedStatus)
                        showDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Отменить")
                }
            },
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colors.background,
            modifier = Modifier.padding(16.dp)
        )
    }
}



fun parseDate(dateString: String): String {
    val instant = Instant.parse(dateString) // Преобразуем в Instant
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss") // Формат вывода
        .withZone(ZoneId.systemDefault()) // Указываем временную зону
    return formatter.format(instant) // Форматируем дату
}