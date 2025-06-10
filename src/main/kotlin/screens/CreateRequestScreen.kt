package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import api.Category
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
        // возможные варианты 'Low', 'Medium', 'High', 'Critical'
        // для пользователя должно отображаться на руссуом
        var priority by remember { mutableStateOf("Medium") }
        var categoryId by remember { mutableIntStateOf(1) }
        val categories = remember { mutableStateListOf<Category>() }

        var isLoading by remember { mutableStateOf(false) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val preferences = remember { Preferences.userRoot().node("user") }
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            RetrofitClient.instance.getAllCategories(
                token = "Bearer " + preferences.get("token", null)
            ).body()?.let {
                categories.addAll(it)
            }
        }

        fun onCreateRequest() {
            scope.launch {
                isLoading = true
                try {
                    val response = RetrofitClient.instance.createRequest(
                        title = title,
                        desc = description,
                        priority = priority,
                        catId = categoryId,
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


        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primary)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Создание новой заявки",
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Поле для заголовка
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = title.isEmpty()
                )

                // Поле для описания
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание проблемы*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    isError = description.isEmpty()
                )

                // Выбор приоритета
                var priorityExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = when (priority) {
                            "Low" -> "Низкий"
                            "Medium" -> "Средний"
                            "High" -> "Высокий"
                            "Critical" -> "Критичный"
                            else -> priority
                        },
                        onValueChange = {},
                        label = { Text("Приоритет") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                if (priorityExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    priorityExpanded = !priorityExpanded
                                }
                            )
                        },
                        readOnly = true,
                        interactionSource = remember { MutableInteractionSource() }
                            .also { source ->
                                if (priorityExpanded) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures {
                                                    priorityExpanded = false
                                                }
                                            }
                                    )
                                }
                            }
                    )

                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false },
                        modifier = Modifier.fillMaxWidth(fraction = 0.9f)
                    ) {
                        listOf("Low" to "Низкий", "Medium" to "Средний", "High" to "Высокий", "Critical" to "Критичный").forEach { (value, text) ->
                            DropdownMenuItem(
                                onClick = {
                                    priority = value
                                    priorityExpanded = false
                                }
                            ) {
                                Text(text)
                            }
                        }
                    }
                }

                // Выбор категории
                var categoryExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categories.find { it.id == categoryId }?.name ?: "Загрузка...",
                        onValueChange = {},
                        label = { Text("Категория") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                if (categoryExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    categoryExpanded = !categoryExpanded
                                }
                            )
                        },
                        readOnly = true,
                        enabled = categories.isNotEmpty()
                    )

                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(fraction = 0.9f)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                onClick = {
                                    categoryId = category.id
                                    categoryExpanded = false
                                }
                            ) {
                                Text(category.name)
                            }
                        }
                    }
                }

                // Кнопка отправки
                Button(
                    onClick = { onCreateRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = title.isNotEmpty() && description.isNotEmpty() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Создать заявку")
                    }
                }
            }
        }

        // Диалоговые окна
        successMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { successMessage = null },
                title = { Text("Успех") },
                text = { Text(message) },
                confirmButton = {
                    Button(onClick = { successMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }

        errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Ошибка") },
                text = { Text(message) },
                confirmButton = {
                    Button(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
