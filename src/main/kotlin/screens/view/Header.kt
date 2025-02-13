package screens.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.UserEntityDto
import api.UserRole

@Composable
fun Header(user: UserEntityDto, onLogout: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp), // Немного отступов для улучшения визуального восприятия
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Аватар пользователя
                Image(
                    painter = painterResource("profile.png"), // Заменить на фактический ресурс
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 16.dp, top = 5.dp, bottom = 5.dp)
                        .clip(CircleShape)
                )

                Column {
                    Text(
                        text = user.username,
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Отображаем роль
                    Text(
                        text = user.role.title,
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                    )
                }
            }
        },
        actions = {
            // Кнопка для выхода
            IconButton(onClick = { onLogout() }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 8.dp, // Тень для глубины
        modifier = Modifier.fillMaxWidth()
    )
}