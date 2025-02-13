import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.RequestEntityDto
import api.RequestStatus
import screens.parseDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RequestCard(request: RequestEntityDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 6.dp,
        onClick = onClick
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Заголовок заявки
                Text(
                    text = request.title,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Описание заявки
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.body2.copy(color = Color.DarkGray)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Информация о статусе с иконкой
                Spacer(modifier = Modifier.height(10.dp))

                // Информация о пользователе
                Text(
                    text = "Отправил: ${request.user.username} (${request.user.email})",
                    style = MaterialTheme.typography.body2.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Дата создания
                Text(
                    text = "Дата: ${parseDate(request.createdAt)}",
                    style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                )
            }

            StatusBadge(request.status)
        }
    }
}

// Компонент для красивого отображения статуса заявки с иконкой
@Composable
fun StatusBadge(status: RequestStatus) {
    val (icon, backgroundColor) = when (status) {
        RequestStatus.Open -> Icons.Default.Info to Color(0xFF4CAF50) // Зеленый
        RequestStatus.InProgress -> Icons.Default.Build to Color(0xFFFFC107) // Желтый
        RequestStatus.Closed -> Icons.Default.Check to Color(0xFFF44336) // Красный
    }

    Row(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp) // Оптимальный размер иконки
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = status.title,
            style = MaterialTheme.typography.body2.copy(color = Color.White),
            fontWeight = FontWeight.W900,
            fontSize = 18.sp
        )
    }
}
