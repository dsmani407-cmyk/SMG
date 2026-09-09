package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CallLogEntity
import com.example.data.model.UserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsTrackingScreen(
    currentUser: UserEntity,
    todayDate: String,
    callLogs: List<CallLogEntity>,
    onSaveCall: (CallLogEntity) -> Unit,
    onDeleteCall: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCall by remember { mutableStateOf<CallLogEntity?>(null) }

    // User's today calls count
    val myTodayCalls = callLogs.filter { it.userId == currentUser.id && it.date == todayDate }
    val myCallsCount = myTodayCalls.size
    val confirmedCount = myTodayCalls.count { it.isConfirmed }

    // Filter calls based on search query
    val filteredLogs = callLogs.filter { log ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.lowercase()
            log.contactName.lowercase().contains(q) ||
            log.phoneNumber.contains(q) ||
            log.callResponse.lowercase().contains(q) ||
            log.scheduledVisitDate.contains(q) ||
            log.userName.lowercase().contains(q)
        }
    }.sortedByDescending { it.id }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header
                item {
                    Text(
                        text = "Daily 20 Prospect Calls & Scheduler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Log call responses, record confirmations, and schedule office visits",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Progress Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Today's Call Target ($todayDate)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "$confirmedCount Confirmed for Office Visit",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "$myCallsCount / 20",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val progress = (myCallsCount.toFloat() / 20f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Search Bar (Requirement 5: List search option)
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by candidate name, phone, response...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("call_logs_search_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Call Logs List
                if (filteredLogs.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneInTalk,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isBlank()) "No calls logged yet today." else "No calls match '$searchQuery'",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Tap the '+' button below to log call #1 to #20.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredLogs) { call ->
                        CallLogCard(
                            call = call,
                            canDelete = currentUser.role == "ADMIN" || currentUser.id == call.userId,
                            onEdit = { editingCall = call },
                            onDelete = { onDeleteCall(call.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Space for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Floating Add Call Button
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_call_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Call")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Call", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Add / Edit Call Dialog
    if (showAddDialog || editingCall != null) {
        val targetCall = editingCall ?: CallLogEntity(
            userId = currentUser.id,
            userName = currentUser.fullName,
            teamId = currentUser.teamId,
            date = todayDate,
            callNumber = (myCallsCount + 1).coerceAtMost(20),
            callTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )

        CallLogEditDialog(
            initialCall = targetCall,
            isNew = editingCall == null,
            onDismiss = {
                showAddDialog = false
                editingCall = null
            },
            onSave = { savedCall ->
                onSaveCall(savedCall)
                showAddDialog = false
                editingCall = null
            }
        )
    }
}

@Composable
fun CallLogCard(
    call: CallLogEntity,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (call.isConfirmed) Color(0xFFF0FDF4) else Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                if (call.isConfirmed) Color(0xFF059669).copy(alpha = 0.15f) else Color(0xFF2563EB).copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${call.callNumber}",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = if (call.isConfirmed) Color(0xFF059669) else Color(0xFF2563EB)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = call.contactName.ifBlank { "Contact #${call.callNumber}" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${call.phoneNumber} • ${call.userName}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Response Tag
                val (tagBg, tagColor) = when (call.callResponse) {
                    "Confirmed Visit" -> Pair(Color(0xFFDCFCE7), Color(0xFF047857))
                    "Interested" -> Pair(Color(0xFFEFF6FF), Color(0xFF1D4ED8))
                    "Callback" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
                    else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = call.callResponse, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tagColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timings & Visit Schedule (Admin & TL can see when call was made, when confirmed, and visit date)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spoken at: ${call.callTime} (${call.date})",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                if (call.isConfirmed && call.confirmationDate.isNotBlank()) {
                    Text(
                        text = "Confirmed: ${call.confirmationDate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF059669)
                    )
                }
            }

            if (call.scheduledVisitDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0F2FE))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scheduled Office Visit: ${call.scheduledVisitDate} ${call.scheduledVisitTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )
                }
            }

            if (call.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Notes: ${call.notes}", fontSize = 12.sp, color = Color(0xFF334155))
            }

            // Edit / Delete Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                }
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CallLogEditDialog(
    initialCall: CallLogEntity,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (CallLogEntity) -> Unit
) {
    var callNumberText by remember { mutableStateOf(initialCall.callNumber.toString()) }
    var name by remember { mutableStateOf(initialCall.contactName) }
    var phone by remember { mutableStateOf(initialCall.phoneNumber) }
    var callTime by remember { mutableStateOf(initialCall.callTime) }
    var response by remember { mutableStateOf(initialCall.callResponse) }
    var scheduledDate by remember { mutableStateOf(initialCall.scheduledVisitDate) }
    var scheduledTime by remember { mutableStateOf(initialCall.scheduledVisitTime) }
    var notes by remember { mutableStateOf(initialCall.notes) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                Text(
                    text = if (isNew) "Log Call #${initialCall.callNumber}" else "Edit Call #${initialCall.callNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Record prospect details and schedule visit date",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = callNumberText,
                        onValueChange = { callNumberText = it },
                        label = { Text("Call # (1-20)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.4f)
                    )
                    OutlinedTextField(
                        value = callTime,
                        onValueChange = { callTime = it },
                        label = { Text("Call Time") },
                        placeholder = { Text("10:30 AM") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("Candidate / Guest Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+91 98765 43210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Call Response / Outcome:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = response == "Confirmed Visit",
                        onClick = {
                            response = "Confirmed Visit"
                            if (scheduledDate.isBlank()) {
                                scheduledDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                scheduledTime = "11:00 AM"
                            }
                        }
                    )
                    Text("Confirmed Visit ✅", fontWeight = FontWeight.Bold, color = Color(0xFF047857), fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    RadioButton(
                        selected = response == "Callback",
                        onClick = { response = "Callback" }
                    )
                    Text("Callback", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = response == "Interested",
                        onClick = { response = "Interested" }
                    )
                    Text("Interested", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    RadioButton(
                        selected = response == "Ringing",
                        onClick = { response = "Ringing" }
                    )
                    Text("Ringing", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = response == "Not Interested",
                        onClick = { response = "Not Interested" }
                    )
                    Text("Not Interested", fontSize = 13.sp)
                }

                // If Confirmed Visit -> Schedule Office Visit Date & Time
                if (response == "Confirmed Visit") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Schedule Office Visit Date & Time*", fontWeight = FontWeight.Bold, color = Color(0xFF065F46), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = scheduledDate,
                                    onValueChange = { scheduledDate = it },
                                    label = { Text("Visit Date") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = scheduledTime,
                                    onValueChange = { scheduledTime = it },
                                    label = { Text("Visit Time") },
                                    placeholder = { Text("11:30 AM") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Conversation Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg ?: "", color = Color(0xFFDC2626), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val num = callNumberText.toIntOrNull() ?: initialCall.callNumber
                            if (name.isBlank()) {
                                errorMsg = "Please enter candidate/guest name."
                            } else if (response == "Confirmed Visit" && scheduledDate.isBlank()) {
                                errorMsg = "Please specify the scheduled office visit date."
                            } else {
                                val isConfirmed = response == "Confirmed Visit"
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                onSave(
                                    initialCall.copy(
                                        callNumber = num,
                                        contactName = name.trim(),
                                        phoneNumber = phone.trim(),
                                        callTime = callTime.trim(),
                                        callResponse = response,
                                        isConfirmed = isConfirmed,
                                        confirmationDate = if (isConfirmed) today else "",
                                        scheduledVisitDate = if (isConfirmed) scheduledDate.trim() else "",
                                        scheduledVisitTime = if (isConfirmed) scheduledTime.trim() else "",
                                        notes = notes.trim(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Save Call Log")
                    }
                }
            }
        }
    }
}
}
