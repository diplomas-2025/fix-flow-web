package screens

import RequestCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import screens.view.Header
import java.util.prefs.Preferences

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var user by remember { mutableStateOf<UserEntityDto?>(null) }
        val requests = remember { mutableStateListOf<RequestEntityDto>() }
        val preferences = remember { Preferences.userRoot().node("user") }

        LaunchedEffect(Unit) {
            user = RetrofitClient.instance.getUser(
                "Bearer " + preferences.get("token", null)
            )

            requests.clear()
            if (user?.role == UserRole.ItSupport) {
                RetrofitClient.instance
                    .getAllRequests("Bearer " + preferences.get("token", null)).let { requests.addAll(it) }
            } else {
                RetrofitClient.instance
                    .getAllRequestsForUser("Bearer " + preferences.get("token", null)).let { requests.addAll(it) }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            user?.let {
                AppHeader(
                    user = it,
                    onLogout = {
                        preferences.remove("token")
                        navigator.replaceAll(AuthScreen())
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Заявки",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (user?.role == UserRole.Employee) {
                Button(
                    onClick = { navigator.push(CreateRequestScreen()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text("+ Новая заявка")
                }
            }

            RequestListSection(
                requests = requests,
                navigator = navigator,
                user = user
            )
        }
    }
}


@Composable
fun AppHeader(user: UserEntityDto, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Добро пожаловать,",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            )
            Text(
                text = user.username,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            )
        }

        IconButton(
            onClick = onLogout,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Выйти",
                tint = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun RequestListSection(
    requests: SnapshotStateList<RequestEntityDto>,
    navigator: Navigator,
    user: UserEntityDto?
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("newest") }

    var selectedStatus by remember { mutableStateOf<RequestStatus?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedPriority by remember { mutableStateOf<String?>(null) }
    var selectedRating by remember { mutableStateOf<Int?>(null) }

    val categories = remember { mutableStateListOf<Category>() }

    val priorities = listOf("Низкий", "Средний", "Высокий", "Критичный")
    val ratings = listOf(1, 2, 3, 4, 5)

    val preferences = remember { Preferences.userRoot().node("user") }

    val filteredRequests = remember(
        requests, requests.size,
        searchQuery, sortOption,
        selectedStatus, selectedCategory, selectedPriority, selectedRating
    ) {
        requests
            .filter { request ->
                (searchQuery.isBlank() ||
                        request.title.contains(searchQuery, ignoreCase = true) ||
                        request.description.contains(searchQuery, ignoreCase = true)) &&
                        (selectedStatus == null || request.status == selectedStatus) &&
                        (selectedCategory == null || request.category == selectedCategory) &&
                        (selectedPriority == null || request.priority == when(selectedPriority) {
                            "Низкий" -> "Low"
                            "Средний" -> "Medium"
                            "Высокий" -> "High"
                            "Критичный" -> "Critical"
                            else -> selectedPriority
                        }) &&
                        (selectedRating == null || request.satisfactionRating == selectedRating)
            }
            .sortedWith(
                when (sortOption) {
                    "newest" -> compareByDescending { it.createdAt }
                    "oldest" -> compareBy { it.createdAt }
                    "title" -> compareBy { it.title }
                    else -> compareByDescending { it.createdAt }
                }
            )
    }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getAllCategories(
            "Bearer " + preferences.get("token", null)
        ).body()?.let {
            categories.clear()
            categories.addAll(it)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF666666)
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color(0xFFDDDDDD)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Фильтры
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                DropdownSelector(
                    label = selectedStatus?.title ?: "Статус",
                    options = RequestStatus.entries.map { it.title },
                    onSelect = { selectedStatus = RequestStatus.entries.find { entry -> entry.title == it } },
                    onClear = { selectedStatus = null }
                )
            }

            item {
                DropdownSelector(
                    label = selectedCategory?.name ?: "Категория",
                    options = categories.map { it.name },
                    onSelect = { value -> selectedCategory = categories.first { it.name == value } },
                    onClear = { selectedCategory = null }
                )
            }

            item {
                DropdownSelector(
                    label = selectedPriority ?: "Приоритет",
                    options = priorities,
                    onSelect = { selectedPriority = it },
                    onClear = { selectedPriority = null }
                )
            }

            item {
                DropdownSelector(
                    label = selectedRating?.toString() ?: "Оценка",
                    options = ratings.map { it.toString() },
                    onSelect = { selectedRating = it.toInt() },
                    onClear = { selectedRating = null }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Сортировка
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сортировка:",
                color = Color(0xFF666666),
                modifier = Modifier.padding(end = 8.dp)
            )

            SortChip("Новые", sortOption == "newest") { sortOption = "newest" }
            SortChip("Старые", sortOption == "oldest") { sortOption = "oldest" }
            SortChip("По названию", sortOption == "title") { sortOption = "title" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список заявок
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredRequests) { request ->
                ModernRequestCard(
                    request = request,
                    onClick = { user?.let { navigator.push(RequestScreen(request.id, user)) } }
                )
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                content = { Text("Все") },
                onClick = {
                    onClear()
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    content = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Chip(
        onClick = onClick,
        modifier = Modifier.padding(end = 8.dp),
        colors = ChipDefaults.chipColors(
            backgroundColor = if (selected) Color(0xFF6200EE) else Color.White,
            contentColor = if (selected) Color.White else Color(0xFF666666)
        ),
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFDDDDDD))
    ) {
        Text(label, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModernRequestCard(
    request: RequestEntityDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = request.title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(
                            color = when (request.status) {
                                RequestStatus.Open -> Color(0xFF2196F3)
                                RequestStatus.InProgress -> Color(0xFFFFC107)
                                RequestStatus.Closed -> Color(0xFF4CAF50)
                            }.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status.title,
                        color = when (request.status) {
                            RequestStatus.Open -> Color(0xFF2196F3)
                            RequestStatus.InProgress -> Color(0xFFFFC107)
                            RequestStatus.Closed -> Color(0xFF4CAF50)
                        },
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = request.description.take(100) + if (request.description.length > 100) "..." else "",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Категория: ${request.category.name}",
                    fontSize = 12.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "Приоритет: ${when (request.priority) {
                        "Low" -> "Низкий"
                        "Medium" -> "Средний"
                        "High" -> "Высокий"
                        "Critical" -> "Критичный"
                        else -> request.priority
                    }}",
                    fontSize = 12.sp,
                    color = Color(0xFF555555)
                )
            }

            if (request.satisfactionRating != null || !request.feedbackText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    request.satisfactionRating?.let {
                        Text(
                            text = "Оценка: $it/5",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    if (!request.feedbackText.isNullOrBlank()) {
                        Text(
                            text = "Отзыв: ${request.feedbackText}",
                            fontSize = 12.sp,
                            color = Color(0xFF777777),
                            maxLines = 3
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "№${request.id}",
                    color = Color(0xFF999999),
                    fontSize = 12.sp
                )

                Text(
                    text = request.createdAt.formatAsDate(),
                    color = Color(0xFF999999),
                    fontSize = 12.sp
                )
            }
        }
    }
}


// Extension function for date formatting
fun String.formatAsDate(): String {
    return this.split('T').first()
}
