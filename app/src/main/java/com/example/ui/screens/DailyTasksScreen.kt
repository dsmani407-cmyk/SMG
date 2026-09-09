package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyTaskEntity
import com.example.data.model.UserEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Model for user-created custom tasks for the day
 */
data class CustomTaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetTime: String = "",
    val completed: Boolean = false
)

fun parseCustomTasksJson(json: String): List<CustomTaskItem> {
    if (json.isBlank()) return emptyList()
    return try {
        val array = JSONArray(json)
        val list = mutableListOf<CustomTaskItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                CustomTaskItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    title = obj.optString("title", ""),
                    targetTime = obj.optString("targetTime", ""),
                    completed = obj.optBoolean("completed", false)
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

fun serializeCustomTasksJson(tasks: List<CustomTaskItem>): String {
    val array = JSONArray()
    tasks.forEach { task ->
        val obj = JSONObject()
        obj.put("id", task.id)
        obj.put("title", task.title)
        obj.put("targetTime", task.targetTime)
        obj.put("completed", task.completed)
        array.put(obj)
    }
    return array.toString()
}

@Composable
fun DailyTasksScreen(
    currentUser: UserEntity,
    todayDate: String,
    tasks: List<DailyTaskEntity>,
    allUsers: List<UserEntity> = emptyList(),
    onSaveTask: (DailyTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    // Admin or Team Leader can select any distributor to view/edit
    val canSelectAnyUser = currentUser.role == "ADMIN" || currentUser.role == "TEAM_LEADER"
    var selectedTargetUser by remember(currentUser) { mutableStateOf(currentUser) }

    // Target user's task for today
    val activeTask = tasks.find { it.userId == selectedTargetUser.id && it.date == todayDate }

    var eveningStatus by remember(activeTask) { mutableStateOf(activeTask?.eveningCheckoutStatus ?: "PENDING") }
    var eveningTime by remember(activeTask) {
        mutableStateOf(
            if (activeTask?.eveningCheckoutTime?.isNotBlank() == true) {
                activeTask.eveningCheckoutTime
            } else {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            }
        )
    }
    var eveningNotes by remember(activeTask) { mutableStateOf(activeTask?.eveningNotes ?: "") }

    // Custom Tasks state - exclusively distributor self-created tasks
    var customTasks by remember(activeTask) {
        mutableStateOf(parseCustomTasksJson(activeTask?.customTasksJson ?: ""))
    }

    // Dialog state for creating new custom task
    var showAddCustomTaskDialog by remember { mutableStateOf(false) }
    var newCustomTaskTitle by remember { mutableStateOf("") }
    var newCustomTaskTime by remember { mutableStateOf("") }

    // Dialog state for editing existing custom task
    var editingCustomTaskIndex by remember { mutableStateOf<Int?>(null) }
    var editCustomTaskTitle by remember { mutableStateOf("") }
    var editCustomTaskTime by remember { mutableStateOf("") }

    var savedMessage by remember { mutableStateOf<String?>(null) }

    // Compute custom task completion
    val completedCount = customTasks.count { it.completed }
    val totalCount = customTasks.size
    val isEverythingDone = totalCount > 0 && completedCount == totalCount

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Screen Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Tasks & Evening Closing",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Create custom tasks, edit daily plan & submit evening Thumbs Up/Down status",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Distributor Selector Bar (For Admin / TL to edit any distributor's tasks)
            if (canSelectAnyUser && allUsers.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Select Distributor to View & Edit Tasks:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                if (selectedTargetUser.id != currentUser.id) {
                                    TextButton(onClick = { selectedTargetUser = currentUser }) {
                                        Text("Switch to Me", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allUsers.filter { it.role != "ADMIN" }) { u ->
                                    val isSelected = u.id == selectedTargetUser.id
                                    val uTask = tasks.find { it.userId == u.id && it.date == todayDate }
                                    val uDone = uTask?.eveningCheckoutStatus == "COMPLETED"

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isSelected) Color(0xFF2563EB)
                                                else if (uDone) Color(0xFFECFDF5)
                                                else Color(0xFFF1F5F9)
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF1D4ED8)
                                                else if (uDone) Color(0xFF10B981)
                                                else Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable { selectedTargetUser = u }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (uDone) "👍 " else "👎 ",
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = u.fullName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White
                                                else if (uDone) Color(0xFF047857)
                                                else Color(0xFF334155)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Distributor Task Editor Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Card Header with User & Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Daily Checklist ($todayDate)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    if (selectedTargetUser.id != currentUser.id) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFEF3C7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Admin Edit Mode", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                        }
                                    }
                                }
                                Text(
                                    text = "Distributor: ${selectedTargetUser.fullName} (${selectedTargetUser.teamName.ifBlank { "Smart Group" }})",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Completion Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isEverythingDone) Color(0xFFDCFCE7) else Color(0xFFEFF6FF))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$completedCount/$totalCount Done",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEverythingDone) Color(0xFF047857) else Color(0xFF2563EB)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Section: Exclusive Custom Distributor Daily Tasks
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Distributor Daily Tasks (Self-Created):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Add, edit, or delete personalized daily targets",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Button(
                                onClick = {
                                    newCustomTaskTitle = ""
                                    newCustomTaskTime = ""
                                    showAddCustomTaskDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Custom Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (customTasks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No custom tasks created for today.",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "Tap '+ Add Custom Task' above to define your targets for today!",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        } else {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                customTasks.forEachIndexed { index, customTask ->
                                    CustomTaskRowWithThumbs(
                                        task = customTask,
                                        onToggle = { isChecked ->
                                            val updatedList = customTasks.toMutableList()
                                            updatedList[index] = customTask.copy(completed = isChecked)
                                            customTasks = updatedList
                                        },
                                        onEdit = {
                                            editingCustomTaskIndex = index
                                            editCustomTaskTitle = customTask.title
                                            editCustomTaskTime = customTask.targetTime
                                        },
                                        onDelete = {
                                            val updatedList = customTasks.toMutableList()
                                            updatedList.removeAt(index)
                                            customTasks = updatedList
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section 3: Evening Task Closing Time & Status (Thumbs Up / Thumbs Down Rule)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (eveningStatus == "COMPLETED" || isEverythingDone) Color(0xFFF0FDF4) else Color(0xFFFFF1F2)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (eveningStatus == "COMPLETED" || isEverythingDone) Color(0xFF86EFAC) else Color(0xFFFDA4AF)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Evening Task Closing Review*",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Complete task = 👍 Thumbs Up  |  Incomplete task = 👎 Thumbs Down",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (eveningStatus == "COMPLETED" || isEverythingDone) Color(0xFF047857) else Color(0xFFBE123C)
                                        )
                                    }

                                    // Dynamic Thumbs Indicator Banner
                                    if (eveningStatus == "COMPLETED" || isEverythingDone) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFF22C55E))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.ThumbUp,
                                                    contentDescription = "Thumbs Up",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "THUMBS UP 👍",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFFE11D48))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.ThumbDown,
                                                    contentDescription = "Thumbs Down",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "THUMBS DOWN 👎",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Radio Selection for Evening Status
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = eveningStatus == "COMPLETED",
                                        onClick = {
                                            eveningStatus = "COMPLETED"
                                            eveningTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                        }
                                    )
                                    Text(
                                        "👍 Complete (Thumbs Up)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    RadioButton(
                                        selected = eveningStatus == "IN_PROGRESS" || eveningStatus == "PENDING",
                                        onClick = {
                                            eveningStatus = "PENDING"
                                        }
                                    )
                                    Text(
                                        "👎 Incomplete (Thumbs Down)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBE123C),
                                        fontSize = 12.sp
                                    )
                                }

                                // Informational message based on state
                                if (eveningStatus == "COMPLETED") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "🎉 Excellent! All daily tasks and closing targets verified. Closing registered with Thumbs Up 👍",
                                            fontSize = 11.sp,
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    val pendingList = customTasks.filter { !it.completed }.map { it.title }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = if (pendingList.isNotEmpty()) {
                                                "⚠️ Incomplete targets: ${pendingList.joinToString(", ")}. Registered with Thumbs Down 👎"
                                            } else {
                                                "⚠️ Evening Closing set to Incomplete / Thumbs Down 👎"
                                            },
                                            fontSize = 11.sp,
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = eveningTime,
                                    onValueChange = { eveningTime = it },
                                    label = { Text("Evening Task Closing Time") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = eveningNotes,
                                    onValueChange = { eveningNotes = it },
                                    label = { Text("Evening Notes & Target Summary") },
                                    placeholder = { Text("e.g. Completed visits, 2 candidates scheduled for tomorrow office visit") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (savedMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = savedMessage ?: "",
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val updated = (activeTask ?: DailyTaskEntity(
                                    userId = selectedTargetUser.id,
                                    userName = selectedTargetUser.fullName,
                                    teamId = selectedTargetUser.teamId,
                                    date = todayDate
                                )).copy(
                                    eveningCheckoutStatus = eveningStatus,
                                    eveningCheckoutTime = eveningTime,
                                    eveningNotes = eveningNotes,
                                    customTasksJson = serializeCustomTasksJson(customTasks),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSaveTask(updated)
                                savedMessage = "Daily task and evening closing for ${selectedTargetUser.fullName} successfully saved!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (eveningStatus == "COMPLETED") Color(0xFF16A34A) else Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_daily_task_btn")
                        ) {
                            Icon(
                                imageVector = if (eveningStatus == "COMPLETED") Icons.Default.ThumbUp else Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (eveningStatus == "COMPLETED") "Save Evening Closing (Thumbs Up 👍)" else "Save Evening Closing (Thumbs Down 👎)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // All Distributors Task Submissions History & Watch Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (canSelectAnyUser) "All Distributors Daily Task Closings" else "Team Daily Task Submissions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Admin watch on evening closing: 👍 Complete vs 👎 Incomplete",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Summary counts of Thumbs Up vs Down
                    val upCount = tasks.count { it.date == todayDate && it.eveningCheckoutStatus == "COMPLETED" }
                    val downCount = tasks.count { it.date == todayDate && it.eveningCheckoutStatus != "COMPLETED" }

                    Row {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👍 $upCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👎 $downCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(tasks) { task ->
                TaskSubmissionCardWithThumbs(
                    task = task,
                    canDelete = currentUser.role == "ADMIN",
                    onDelete = { onDeleteTask(task.id) },
                    onEdit = {
                        val target = allUsers.find { it.id == task.userId }
                        if (target != null) {
                            selectedTargetUser = target
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Add Custom Task Dialog
    if (showAddCustomTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomTaskDialog = false },
            title = {
                Text(
                    text = "Create Own Daily Task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Add a custom task for today to track in your daily checklist:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCustomTaskTitle,
                        onValueChange = { newCustomTaskTitle = it },
                        label = { Text("Task Description / Target*") },
                        placeholder = { Text("e.g. Visit Candidate Anand at Guindy") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newCustomTaskTime,
                        onValueChange = { newCustomTaskTime = it },
                        label = { Text("Target Completion Time (Optional)") },
                        placeholder = { Text("e.g. 11:30 AM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomTaskTitle.isNotBlank()) {
                            val newItem = CustomTaskItem(
                                title = newCustomTaskTitle.trim(),
                                targetTime = newCustomTaskTime.trim(),
                                completed = false
                            )
                            customTasks = customTasks + newItem
                            showAddCustomTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Add to Checklist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Custom Task Dialog
    if (editingCustomTaskIndex != null) {
        val editIdx = editingCustomTaskIndex!!
        AlertDialog(
            onDismissRequest = { editingCustomTaskIndex = null },
            title = {
                Text(
                    text = "Edit Daily Task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Modify your custom task description or target time:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editCustomTaskTitle,
                        onValueChange = { editCustomTaskTitle = it },
                        label = { Text("Task Description / Target*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editCustomTaskTime,
                        onValueChange = { editCustomTaskTime = it },
                        label = { Text("Target Completion Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editCustomTaskTitle.isNotBlank() && editIdx in customTasks.indices) {
                            val updatedList = customTasks.toMutableList()
                            updatedList[editIdx] = updatedList[editIdx].copy(
                                title = editCustomTaskTitle.trim(),
                                targetTime = editCustomTaskTime.trim()
                            )
                            customTasks = updatedList
                            editingCustomTaskIndex = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Update Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCustomTaskIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Custom user-created task row with Thumbs Up/Down and edit/delete action
 */
@Composable
fun CustomTaskRowWithThumbs(
    task: CustomTaskItem,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (task.completed) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
            .border(1.dp, if (task.completed) Color(0xFFBBF7D0) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .clickable { onToggle(!task.completed) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
            )
            Column {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = if (task.completed) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (task.completed) Color(0xFF0F172A) else Color(0xFF334155)
                )
                if (task.targetTime.isNotBlank()) {
                    Text(
                        text = "Target: ${task.targetTime}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (task.completed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("👍 Done", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("👎 Pending", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit custom task",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete custom task",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

/**
 * History card for an individual distributor's daily task submission
 */
@Composable
fun TaskSubmissionCardWithThumbs(
    task: DailyTaskEntity,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val isCompleted = task.eveningCheckoutStatus == "COMPLETED"
    val customList = parseCustomTasksJson(task.customTasksJson)
    val customDoneCount = customList.count { it.completed }
    val totalDone = customDoneCount
    val totalAll = customList.size

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) Color(0xFFBBF7D0) else Color(0xFFFECDD3)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($totalDone/$totalAll targets done)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) Color(0xFF15803D) else Color(0xFFDC2626)
                        )
                    }
                    Text(
                        text = "Date: ${task.date} • Evening Closing: ${if (task.eveningCheckoutTime.isNotBlank()) task.eveningCheckoutTime else "Not specified"}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Thumbs Up or Thumbs Down Badge
                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbUp,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("THUMBS UP 👍", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbDown,
                                    contentDescription = null,
                                    tint = Color(0xFFB91C1C),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("THUMBS DOWN 👎", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Task", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                    }

                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Custom task breakdown for this distributor
            if (customList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    customList.forEach { ct ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${ct.title}${if (ct.targetTime.isNotBlank()) " (${ct.targetTime})" else ""}",
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (ct.completed) "👍 Completed" else "👎 Pending",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ct.completed) Color(0xFF15803D) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            if (task.eveningNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Evening Notes: ${task.eveningNotes}",
                    fontSize = 12.sp,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

@Composable
fun StatusMiniChip(label: String, done: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (done) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (done) "✓ $label" else "✗ $label",
            fontSize = 10.sp,
            fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
            color = if (done) Color(0xFF15803D) else Color(0xFF64748B)
        )
    }
}
