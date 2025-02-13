package screens

import RequestCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
            if (user!!.role == UserRole.ItSupport)
                requests.addAll(RetrofitClient.instance.getAllRequests("Bearer " + preferences.get("token", null)))
            else
                requests.addAll(RetrofitClient.instance.getAllRequestsForUser("Bearer " + preferences.get("token", null)))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            user?.let {
                Header(
                    it,
                    onLogout = {
                        preferences.remove("token")
                        navigator.push(AuthScreen())
                    }
                )
            }

            Text(
                text = "Список заявок в технический отдел",
                style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.primary),
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (user?.role == UserRole.Employee) {
                Button(
                    onClick = {
                        navigator.push(CreateRequestScreen())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    Text(
                        text = "Создать новую заявку",
                        style = MaterialTheme.typography.button.copy(color = MaterialTheme.colors.onPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            user?.let { user ->
                RequestListScreen(
                    requests = requests,
                    navigator = navigator,
                    user = user
                )
            }
        }
    }
}

@Composable
fun RequestListScreen(requests: List<RequestEntityDto>, navigator: Navigator, user: UserEntityDto) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<RequestStatus?>(null) }
    var sortOption by remember { mutableStateOf("Дата создания") }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    // Фильтрация и сортировка списка заявок
    val filteredRequests = requests
        .filter { request ->
            (searchQuery.isBlank() || request.title.contains(searchQuery, ignoreCase = true) || request.description.contains(searchQuery, ignoreCase = true)) &&
                    (selectedStatus == null || request.status == selectedStatus)
        }
        .sortedBy {
            when (sortOption) {
                "Дата создания" -> it.createdAt
                "Название" -> it.title
                else -> it.createdAt
            }
        }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск по названию или описанию") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {}),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Фильтр по статусу
            DropdownMenuButton(
                label = "Фильтр: ${selectedStatus?.title ?: "Все"}",
                options = listOf(null) + RequestStatus.entries,
                selectedOption = selectedStatus,
                onOptionSelected = { selectedStatus = it },
                optionLabel = { it?.title ?: "Все" } // Обрабатываем null отдельно
            )


            // Сортировка
            Box {
                Button(onClick = { isSortMenuExpanded = true }) {
                    Text("Сортировка: $sortOption")
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = { isSortMenuExpanded = false }
                ) {
                    listOf("Дата создания", "Название").forEach { option ->
                        DropdownMenuItem(onClick = {
                            sortOption = option
                            isSortMenuExpanded = false
                        }) {
                            Text(option)
                        }
                    }
                }
            }
        }

        // Сетка с карточками заявок
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(filteredRequests) { request ->
                RequestCard(request = request, onClick = {
                    navigator.push(RequestScreen(request.id, user))
                })
            }
        }
    }
}

// Универсальная кнопка для фильтрации
@Composable
fun <T> DropdownMenuButton(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    optionLabel: (T?) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(label)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(optionLabel(option))
                }
            }
        }
    }
}
