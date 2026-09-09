package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdPostEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CallLogEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.TeamEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.SlateBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminMasterScreen(
    currentUser: UserEntity,
    allUsers: List<UserEntity>,
    allTeams: List<TeamEntity>,
    allGuests: List<GuestPipelineEntity>,
    allTasks: List<DailyTaskEntity> = emptyList(),
    allAdPosts: List<AdPostEntity> = emptyList(),
    allCalls: List<CallLogEntity> = emptyList(),
    auditLogs: List<AuditLogEntity>,
    isCloudSyncing: Boolean,
    lastSyncTime: String,
    todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    onBack: () -> Unit,
    onTriggerCloudSync: () -> Unit,
    onDeleteGuest: (Long) -> Unit,
    onDeleteUser: (String) -> Unit,
    onSaveTask: (DailyTaskEntity) -> Unit = {},
    onDeleteTask: (Long) -> Unit = {}
) {
    var selectedSection by remember { mutableStateOf(0) }
    val sections = listOf(
        "A to Z Watch",
        "10-Step Pipeline",
        "Daily Tasks & Closings",
        "5 Ad Postings",
        "20 Calling Logs",
        "Teams & Staff",
        "Cloud & Security Logs"
    )

    // Financial rollups
    val totalRevenue = allGuests.filter { it.salesClosed }.sumOf { it.totalPackageAmount }
    val totalSeniority = allGuests.filter { it.seniorityCompleted }.sumOf { it.seniorityAmount }
    val totalRegistrations = allGuests.count { it.registrationFeePaid }

    // Today's Operational Status
    val todayTasks = allTasks.filter { it.date == todayDate }
    val thumbsUpTasks = todayTasks.count { it.eveningCheckoutStatus == "COMPLETED" }
    val thumbsDownTasks = todayTasks.count { it.eveningCheckoutStatus != "COMPLETED" }

    val todayAds = allAdPosts.filter { it.date == todayDate }
    val postedAdsCount = todayAds.count { it.isPosted }

    val todayCalls = allCalls.filter { it.date == todayDate }
    val confirmedVisitsCount = todayCalls.count { it.isConfirmed || it.callResponse == "Confirmed Visit" }

    // State for Admin Task Editing Dialog
    var editingTaskDistributor by remember { mutableStateOf<UserEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(14.dp)
    ) {
        // Master Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Navy900)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Admin A to Z Command Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Navy900
                    )
                    Text(
                        text = "Overall Operations • Real-Time Firestore Sync Active",
                        fontSize = 11.sp,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onTriggerCloudSync,
                colors = ButtonDefaults.buttonColors(containerColor = if (isCloudSyncing) GoldAccent else Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("admin_sync_button")
            ) {
                Icon(
                    imageVector = if (isCloudSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isCloudSyncing) "Syncing..." else "Sync Cloud", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cloud & System Health Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cloud Database: Firebase Firestore",
                            color = Color(0xFF4ADE80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "Persistent Offline Cache Active • Synced: $lastSyncTime",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "${allUsers.size} Staff • ${allGuests.size} Guests",
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Tabs for A to Z Watch
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = RoyalBluePrimary,
            divider = {}
        ) {
            sections.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedSection == idx,
                    onClick = { selectedSection = idx },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSection == idx) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedSection) {
            // ----------------------------------------------------
            // TAB 0: A TO Z OPERATIONS WATCH (MASTER DASHBOARD)
            // ----------------------------------------------------
            0 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Live KPI Ribbon
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Today's A to Z Operational Metrics ($todayDate)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Navy900
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Daily Task Status: Thumbs Up vs Thumbs Down
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AdminKpiBadge(
                                        title = "Evening Closings",
                                        mainText = "👍 $thumbsUpTasks  |  👎 $thumbsDownTasks",
                                        subText = if (thumbsDownTasks > 0) "$thumbsDownTasks pending review" else "All completed!",
                                        color = if (thumbsDownTasks > 0) Color(0xFFE11D48) else Color(0xFF16A34A),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminKpiBadge(
                                        title = "5-Source Ad Posts",
                                        mainText = "$postedAdsCount Posts",
                                        subText = "WA, FB, IG, OLX, Job",
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AdminKpiBadge(
                                        title = "20 Calls & Visits",
                                        mainText = "${todayCalls.size} Calls",
                                        subText = "$confirmedVisitsCount visits booked",
                                        color = Color(0xFF7C3AED),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminKpiBadge(
                                        title = "Pipeline Candidates",
                                        mainText = "${allGuests.size} Total",
                                        subText = "${allGuests.count { it.salesClosed }} closed sales",
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Organization Financials Rollup
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Cross-Team Organization Financials",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Navy900
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatBox(
                                        title = "Closed Revenue",
                                        value = "₹$totalRevenue",
                                        color = EmeraldSuccess,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatBox(
                                        title = "Seniority Coll.",
                                        value = "₹$totalSeniority",
                                        color = GoldAccent,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatBox(
                                        title = "₹150 Reg Fees",
                                        value = "₹${totalRegistrations * 150}",
                                        color = RoyalBluePrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Master Distributor A to Z Matrix
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Distributor A-to-Z Execution Status:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Navy900
                            )
                            Text(
                                text = "Click 'Edit' to modify distributor tasks",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    val distributors = allUsers.filter { it.role != "ADMIN" }
                    items(distributors) { dist ->
                        val distTask = todayTasks.find { it.userId == dist.id }
                        val distAds = todayAds.filter { it.userId == dist.id }
                        val distCalls = todayCalls.filter { it.userId == dist.id }
                        val distGuests = allGuests.filter { it.distributorId == dist.id }

                        DistributorAtoZCard(
                            user = dist,
                            task = distTask,
                            adPosts = distAds,
                            callLogs = distCalls,
                            guestCount = distGuests.size,
                            onEditTasks = { editingTaskDistributor = dist }
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // TAB 1: 10-STEP CANDIDATE PIPELINE
            // ----------------------------------------------------
            1 -> {
                var pipelineFilter by remember { mutableStateOf("ALL") }
                var candidateSearch by remember { mutableStateOf("") }

                Column(modifier = Modifier.weight(1f)) {
                    // Search & Filter Row
                    OutlinedTextField(
                        value = candidateSearch,
                        onValueChange = { candidateSearch = it },
                        placeholder = { Text("Search candidate name, phone or distributor...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val filters = listOf(
                            "ALL" to "All (${allGuests.size})",
                            "REG_PAID" to "₹150 Paid (${allGuests.count { it.registrationFeePaid }})",
                            "COUNSELLING" to "Counselling (${allGuests.count { it.counsellingCompleted }})",
                            "SENIORITY" to "Seniority (${allGuests.count { it.seniorityCompleted }})",
                            "CLOSED" to "Closed Sales (${allGuests.count { it.salesClosed }})"
                        )
                        items(filters) { (key, label) ->
                            val isSel = pipelineFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSel) Color(0xFF0F172A) else Color.White)
                                    .border(1.dp, if (isSel) Color(0xFF0F172A) else Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                                    .clickable { pipelineFilter = key }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredGuests = allGuests.filter { g ->
                        val matchesSearch = candidateSearch.isBlank() ||
                                g.guestName.contains(candidateSearch, ignoreCase = true) ||
                                g.phoneNumber.contains(candidateSearch, ignoreCase = true) ||
                                g.distributorName.contains(candidateSearch, ignoreCase = true)

                        val matchesFilter = when (pipelineFilter) {
                            "REG_PAID" -> g.registrationFeePaid
                            "COUNSELLING" -> g.counsellingCompleted
                            "SENIORITY" -> g.seniorityCompleted
                            "CLOSED" -> g.salesClosed
                            else -> true
                        }
                        matchesSearch && matchesFilter
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredGuests) { guest ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${guest.guestName} (${guest.phoneNumber})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Navy900
                                            )
                                            Text(
                                                text = "Distributor: ${guest.distributorName} • Team: ${guest.teamId}",
                                                fontSize = 11.sp,
                                                color = RoyalBluePrimary
                                            )
                                        }

                                        IconButton(onClick = { onDeleteGuest(guest.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseError)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // 10-Step Lifecycle Status Badges
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PipelineStepChip(
                                            stepName = "1. Reg ₹150",
                                            done = guest.registrationFeePaid,
                                            detail = if (guest.registrationFeePaid) "${guest.registrationPaymentMode} #${guest.registrationTxnRef.take(6)}" else "Pending"
                                        )
                                        PipelineStepChip(
                                            stepName = "2. Counselling",
                                            done = guest.counsellingCompleted,
                                            detail = guest.counsellorName.ifBlank { "Unassigned" }
                                        )
                                        PipelineStepChip(
                                            stepName = "3. Seniority",
                                            done = guest.seniorityCompleted,
                                            detail = if (guest.seniorityCompleted) "₹${guest.seniorityAmount}" else "None"
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PipelineStepChip(
                                            stepName = "4. Follow-up",
                                            done = guest.followUpCount > 0,
                                            detail = "Stage ${guest.followUpCount}/3"
                                        )
                                        PipelineStepChip(
                                            stepName = "5. Sales Closed",
                                            done = guest.salesClosed,
                                            detail = if (guest.salesClosed) "₹${guest.totalPackageAmount}" else "Pending"
                                        )
                                    }

                                    if (guest.salesClosed) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFDCFCE7))
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "🎉 FINAL SALES CLOSED: ₹${guest.totalPackageAmount} (${guest.finalPaymentMode.replace("_", " ")}) • Ref: ${guest.finalTxnRef}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // TAB 2: DAILY TASKS & EVENING CLOSINGS WATCH
            // ----------------------------------------------------
            2 -> {
                var taskFilter by remember { mutableStateOf("ALL") }

                Column(modifier = Modifier.weight(1f)) {
                    // Filter Chips: All, 👍 Thumbs Up, 👎 Thumbs Down
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { taskFilter = "ALL" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (taskFilter == "ALL") Color(0xFF0F172A) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("All (${allTasks.size})", color = if (taskFilter == "ALL") Color.White else Color(0xFF334155), fontSize = 12.sp)
                        }

                        Button(
                            onClick = { taskFilter = "UP" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (taskFilter == "UP") Color(0xFF16A34A) else Color(0xFFDCFCE7)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (taskFilter == "UP") Color.White else Color(0xFF15803D))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("👍 Thumbs Up (${allTasks.count { it.eveningCheckoutStatus == "COMPLETED" }})", color = if (taskFilter == "UP") Color.White else Color(0xFF15803D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { taskFilter = "DOWN" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (taskFilter == "DOWN") Color(0xFFE11D48) else Color(0xFFFEE2E2)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.ThumbDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (taskFilter == "DOWN") Color.White else Color(0xFFB91C1C))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("👎 Thumbs Down (${allTasks.count { it.eveningCheckoutStatus != "COMPLETED" }})", color = if (taskFilter == "DOWN") Color.White else Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredTasks = allTasks.filter {
                        when (taskFilter) {
                            "UP" -> it.eveningCheckoutStatus == "COMPLETED"
                            "DOWN" -> it.eveningCheckoutStatus != "COMPLETED"
                            else -> true
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTasks) { task ->
                            val isComplete = task.eveningCheckoutStatus == "COMPLETED"
                            val targetDist = allUsers.find { it.id == task.userId }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isComplete) Color(0xFFBBF7D0) else Color(0xFFFECDD3)
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
                                            Text(
                                                text = task.userName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Navy900
                                            )
                                            Text(
                                                text = "Date: ${task.date} • Closing Time: ${if (task.eveningCheckoutTime.isNotBlank()) task.eveningCheckoutTime else "Pending"}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isComplete) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFDCFCE7))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
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
                                                        Icon(Icons.Filled.ThumbDown, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("THUMBS DOWN 👎", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            IconButton(
                                                onClick = { if (targetDist != null) editingTaskDistributor = targetDist },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Task", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = { onDeleteTask(task.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Breakdown chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AdminStatusChip(label = "Planning", done = task.morningPlanning)
                                        AdminStatusChip(label = "5 Ads", done = task.adPostingsDone)
                                        AdminStatusChip(label = "20 Calls", done = task.twentyCallsDone)
                                        AdminStatusChip(label = "Follow-ups", done = task.guestFollowUpsDone)
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
                    }
                }
            }

            // ----------------------------------------------------
            // TAB 3: 5 AD POSTINGS WATCH
            // ----------------------------------------------------
            3 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "All Distributors 5-Source Ad Postings ($todayDate):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                    }

                    items(allAdPosts.sortedByDescending { it.date }) { post ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = post.sourceName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Navy900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Slot #${post.sourceIndex}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Text(
                                        text = "Posted by: ${post.userName} • Date: ${post.date} ${post.postTime}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2563EB)
                                    )
                                    if (post.postDetails.isNotBlank()) {
                                        Text(
                                            text = "Proof/Link: ${post.postDetails}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }

                                if (post.isPosted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("✓ Posted", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("✗ Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // TAB 4: 20 CALLING LOGS WATCH
            // ----------------------------------------------------
            4 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "20 Daily Calling Records Across Teams ($todayDate):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                    }

                    items(allCalls.sortedByDescending { it.date }) { call ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${call.contactName.ifBlank { "Candidate" }} (${call.phoneNumber})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Navy900
                                        )
                                        Text(
                                            text = "Caller: ${call.userName} • Date: ${call.date} ${call.callTime}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    val isConfirmed = call.isConfirmed || call.callResponse == "Confirmed Visit"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isConfirmed) Color(0xFFDCFCE7)
                                                else if (call.callResponse == "Interested") Color(0xFFEFF6FF)
                                                else Color(0xFFF1F5F9)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = call.callResponse,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isConfirmed) Color(0xFF15803D)
                                            else if (call.callResponse == "Interested") Color(0xFF2563EB)
                                            else Color(0xFF475569)
                                        )
                                    }
                                }

                                if (call.scheduledVisitDate.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📅 Scheduled Office Visit: ${call.scheduledVisitDate} at ${call.scheduledVisitTime}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF047857)
                                    )
                                }

                                if (call.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Notes: ${call.notes}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // TAB 5: TEAMS & USERS
            // ----------------------------------------------------
            5 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Active Teams & Leadership Hierarchy:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                    }

                    items(allTeams) { team ->
                        val teamUsers = allUsers.filter { it.teamId == team.id }
                        val teamGuests = allGuests.filter { it.teamId == team.id }
                        val teamSales = teamGuests.filter { it.salesClosed }.sumOf { it.totalPackageAmount }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = team.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Navy900
                                    )
                                    Text(
                                        text = "${teamUsers.size} Distributors",
                                        color = RoyalBluePrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Leader: ${team.leaderName} • Total Pipeline: ${teamGuests.size} • Revenue: ₹$teamSales",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Staff & Distributor Directory (${allUsers.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                    }

                    items(allUsers) { user ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Navy900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (user.role == "ADMIN") Color(0xFFFEF3C7) else Color(0xFFEFF6FF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = user.role,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (user.role == "ADMIN") Color(0xFFB45309) else Color(0xFF2563EB)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Team: ${user.teamName} • Phone: ${user.phone} • Email: ${user.email}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                if (user.role != "ADMIN") {
                                    IconButton(onClick = { onDeleteUser(user.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseError)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // TAB 6: CLOUD & SECURITY LOGS
            // ----------------------------------------------------
            6 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Firestore Cloud Synchronization Status", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Engine: Firebase Firestore Persistent Cache", fontSize = 11.sp, color = Color(0xFF475569))
                                Text("• Real-time synchronization active across all devices", fontSize = 11.sp, color = Color(0xFF475569))
                                Text("• Last sync executed: $lastSyncTime", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(auditLogs) { log ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.action,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = RoyalBluePrimary
                                    )
                                    val dateStr = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                    Text(
                                        text = dateStr,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = log.details,
                                    fontSize = 11.sp,
                                    color = Navy900
                                )
                                Text(
                                    text = "By: ${log.userName} (${log.userId})",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Admin Task Quick Editor Dialog
    editingTaskDistributor?.let { targetDist ->
        val currentDistTask = allTasks.find { it.userId == targetDist.id && it.date == todayDate }
        AdminEditTaskDialog(
            targetUser = targetDist,
            existingTask = currentDistTask,
            todayDate = todayDate,
            onDismiss = { editingTaskDistributor = null },
            onSave = { updatedTask ->
                onSaveTask(updatedTask)
                editingTaskDistributor = null
            }
        )
    }
}

/**
 * Individual Distributor Status Card inside the A to Z Matrix
 */
@Composable
fun DistributorAtoZCard(
    user: UserEntity,
    task: DailyTaskEntity?,
    adPosts: List<AdPostEntity>,
    callLogs: List<CallLogEntity>,
    guestCount: Int,
    onEditTasks: () -> Unit
) {
    val isComplete = task?.eveningCheckoutStatus == "COMPLETED"
    val postedAds = adPosts.count { it.isPosted }
    val loggedCalls = callLogs.size

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Navy900
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${user.teamName.ifBlank { "Team Member" }})",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = "Phone: ${user.phone}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Evening Thumbs Up or Thumbs Down Badge
                if (isComplete) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(13.dp))
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
                            Icon(Icons.Filled.ThumbDown, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("THUMBS DOWN 👎", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Operational metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricChip(label = "Ads: $postedAds/5", done = postedAds >= 5)
                MetricChip(label = "Calls: $loggedCalls/20", done = loggedCalls >= 20)
                MetricChip(label = "Pipeline: $guestCount", done = guestCount > 0)
                if (task?.eveningCheckoutTime?.isNotBlank() == true) {
                    MetricChip(label = "Time: ${task.eveningCheckoutTime}", done = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task?.eveningNotes?.isNotBlank() == true) {
                    Text(
                        text = "Notes: ${task.eveningNotes}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = if (isComplete) "All evening targets verified" else "Evening closing pending verification",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = onEditTasks,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Tasks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Admin Dialog to Edit any Distributor's Daily Task
 */
@Composable
fun AdminEditTaskDialog(
    targetUser: UserEntity,
    existingTask: DailyTaskEntity?,
    todayDate: String,
    onDismiss: () -> Unit,
    onSave: (DailyTaskEntity) -> Unit
) {
    var morningPlan by remember { mutableStateOf(existingTask?.morningPlanning ?: false) }
    var adDone by remember { mutableStateOf(existingTask?.adPostingsDone ?: false) }
    var callsDone by remember { mutableStateOf(existingTask?.twentyCallsDone ?: false) }
    var followUpsDone by remember { mutableStateOf(existingTask?.guestFollowUpsDone ?: false) }
    var eveningStatus by remember { mutableStateOf(existingTask?.eveningCheckoutStatus ?: "PENDING") }
    var eveningTime by remember {
        mutableStateOf(
            if (existingTask?.eveningCheckoutTime?.isNotBlank() == true) existingTask.eveningCheckoutTime
            else SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
    }
    var eveningNotes by remember { mutableStateOf(existingTask?.eveningNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Daily Tasks: ${targetUser.fullName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Update daily requirements & evening Thumbs Up/Down status:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(10.dp))

                TaskCheckItemRow(title = "Morning Planning", checked = morningPlan, onCheckedChange = { morningPlan = it })
                TaskCheckItemRow(title = "5 Ad Postings", checked = adDone, onCheckedChange = { adDone = it })
                TaskCheckItemRow(title = "20 Calls Logged", checked = callsDone, onCheckedChange = { callsDone = it })
                TaskCheckItemRow(title = "Guest Follow-ups", checked = followUpsDone, onCheckedChange = { followUpsDone = it })

                Spacer(modifier = Modifier.height(10.dp))

                Text("Evening Closing Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = eveningStatus == "COMPLETED",
                        onClick = { eveningStatus = "COMPLETED" }
                    )
                    Text("👍 Thumbs Up (Complete)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))

                    Spacer(modifier = Modifier.width(8.dp))

                    RadioButton(
                        selected = eveningStatus != "COMPLETED",
                        onClick = { eveningStatus = "PENDING" }
                    )
                    Text("👎 Thumbs Down (Incomplete)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = eveningTime,
                    onValueChange = { eveningTime = it },
                    label = { Text("Closing Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = eveningNotes,
                    onValueChange = { eveningNotes = it },
                    label = { Text("Admin / Evening Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = (existingTask ?: DailyTaskEntity(
                        userId = targetUser.id,
                        userName = targetUser.fullName,
                        teamId = targetUser.teamId,
                        date = todayDate
                    )).copy(
                        morningPlanning = morningPlan,
                        adPostingsDone = adDone,
                        twentyCallsDone = callsDone,
                        guestFollowUpsDone = followUpsDone,
                        eveningCheckoutStatus = eveningStatus,
                        eveningCheckoutTime = eveningTime,
                        eveningNotes = eveningNotes,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (eveningStatus == "COMPLETED") Color(0xFF16A34A) else Color(0xFF2563EB)
                )
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TaskCheckItemRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = title, fontSize = 12.sp, color = Navy900)
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = title, fontSize = 11.sp, color = Navy800, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun AdminStatusChip(label: String, done: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
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

@Composable
fun AdminKpiBadge(
    title: String,
    mainText: String,
    subText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = title, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = mainText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subText, fontSize = 10.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun MetricChip(label: String, done: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (done) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
            color = if (done) Color(0xFF15803D) else Color(0xFF475569)
        )
    }
}

@Composable
fun PipelineStepChip(stepName: String, done: Boolean, detail: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (done) Color(0xFFDCFCE7) else Color(0xFFF8FAFC))
            .border(1.dp, if (done) Color(0xFFBBF7D0) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Column {
            Text(text = stepName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (done) Color(0xFF15803D) else Color(0xFF64748B))
            Text(text = detail, fontSize = 9.sp, color = if (done) Color(0xFF047857) else Color(0xFF94A3B8))
        }
    }
}
