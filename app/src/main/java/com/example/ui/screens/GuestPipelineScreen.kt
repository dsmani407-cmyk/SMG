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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.UserEntity
import com.example.data.model.parseSeniorityPaymentsJson
import com.example.ui.components.AssignCounsellorDialog
import com.example.ui.components.FollowUpDialog
import com.example.ui.components.ProcessFileDialog
import com.example.ui.components.RegistrationRefundDialog
import com.example.ui.components.SalesClosingDialog
import com.example.ui.components.SeniorityDialog
import com.example.ui.components.SeniorityRefundDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GuestPipelineScreen(
    currentUser: UserEntity,
    guests: List<GuestPipelineEntity>,
    allDistributors: List<UserEntity> = emptyList(),
    onSaveGuest: (GuestPipelineEntity) -> Unit,
    onDeleteGuest: (Long) -> Unit,
    onAttemptStartCounselling: (GuestPipelineEntity, UserEntity) -> Unit,
    onCompleteSeniority: (guest: GuestPipelineEntity, amount: Double, mode: String, ref: String) -> Unit,
    onAddSeniorityPayment: ((guest: GuestPipelineEntity, amount: Double, mode: String, ref: String, notes: String, voucherCollected: Boolean, voucherNumber: String) -> Unit)? = null,
    onRefundRegistration: ((guest: GuestPipelineEntity, reason: String) -> Unit)? = null,
    onRefundSeniority: ((guest: GuestPipelineEntity, amount: Double, reason: String) -> Unit)? = null,
    onAddFollowUp: (guest: GuestPipelineEntity, notes: String) -> Unit,
    onAddFollowUpWithOutcome: ((guest: GuestPipelineEntity, notes: String, outcome: String, notSaleReason: String) -> Unit)? = null,
    onCompleteSalesClosing: (guest: GuestPipelineEntity, amount: Double, mode: String, ref: String, notes: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ARRIVAL_150, COUNSELLING_L2, SENIORITY, FOLLOW_UP, CLOSED

    // Action dialog states
    var processFileGuest by remember { mutableStateOf<GuestPipelineEntity?>(null) }
    var seniorityGuest by remember { mutableStateOf<GuestPipelineEntity?>(null) }
    var followUpGuest by remember { mutableStateOf<GuestPipelineEntity?>(null) }
    var closingGuest by remember { mutableStateOf<GuestPipelineEntity?>(null) }
    var refundRegGuest by remember { mutableStateOf<GuestPipelineEntity?>(null) }
    var refundSeniorityGuest by remember { mutableStateOf<GuestPipelineEntity?>(null) }
    var assignCounsellorGuest by remember { mutableStateOf<GuestPipelineEntity?>(null) }
    var showManualAddGuest by remember { mutableStateOf(false) }

    // Counselling count map for Rule 7
    val counsellingCountMap = remember(guests) {
        val map = mutableMapOf<String, Int>()
        guests.forEach { g ->
            if (g.counsellorId.isNotBlank()) {
                map[g.counsellorId] = (map[g.counsellorId] ?: 0) + 1
            }
        }
        map
    }

    // Filtered pipeline list
    val filteredGuests = guests.filter { g ->
        val matchesQuery = if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.lowercase()
            g.guestName.lowercase().contains(q) ||
            g.phoneNumber.contains(q) ||
            g.distributorName.lowercase().contains(q) ||
            g.temporaryId.lowercase().contains(q) ||
            g.fileProcessedBy.lowercase().contains(q)
        }
        val matchesFilter = when (selectedFilter) {
            "ARRIVAL_150" -> !g.registrationFeePaid
            "COUNSELLING_L2" -> g.registrationFeePaid && !g.seniorityCompleted
            "SENIORITY" -> g.counsellingCompleted && !g.seniorityCompleted
            "FOLLOW_UP" -> g.seniorityCompleted && !g.salesClosed
            "CLOSED" -> g.salesClosed
            else -> true
        }
        matchesQuery && matchesFilter
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
                item {
                    Text(
                        text = "Candidate Recruitment & Sales Funnel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Office Arrival → ₹150 File Fee → L2 Counselling (Max 7) → Seniority ID → 3x L3 Follow-ups → Sales Closing",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search by candidate, phone, Temp ID, distributor...") },
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
                            .testTag("pipeline_search_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Funnel Stage Filter Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                label = "All (${guests.size})",
                                selected = selectedFilter == "ALL",
                                onClick = { selectedFilter = "ALL" }
                            )
                        }
                        item {
                            FilterChip(
                                label = "₹150 Registration (${guests.count { !it.registrationFeePaid }})",
                                selected = selectedFilter == "ARRIVAL_150",
                                onClick = { selectedFilter = "ARRIVAL_150" }
                            )
                        }
                        item {
                            FilterChip(
                                label = "L2 Counselling (${guests.count { it.registrationFeePaid && !it.seniorityCompleted }})",
                                selected = selectedFilter == "COUNSELLING_L2",
                                onClick = { selectedFilter = "COUNSELLING_L2" }
                            )
                        }
                        item {
                            FilterChip(
                                label = "3x Follow-up (${guests.count { it.seniorityCompleted && !it.salesClosed }})",
                                selected = selectedFilter == "FOLLOW_UP",
                                onClick = { selectedFilter = "FOLLOW_UP" }
                            )
                        }
                        item {
                            FilterChip(
                                label = "Sales Closed (${guests.count { it.salesClosed }})",
                                selected = selectedFilter == "CLOSED",
                                onClick = { selectedFilter = "CLOSED" }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (filteredGuests.isEmpty()) {
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
                                Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No candidates found in this stage.",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Confirm 20 calls to schedule visits, or tap '+' below to add directly.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredGuests) { guest ->
                        GuestPipelineCard(
                            guest = guest,
                            currentUser = currentUser,
                            counsellingCountMap = counsellingCountMap,
                            onProcessFile = { processFileGuest = guest },
                            onRefundRegistration = { refundRegGuest = guest },
                            onStartCounselling = {
                                if (allDistributors.isNotEmpty()) {
                                    assignCounsellorGuest = guest
                                } else {
                                    onAttemptStartCounselling(guest, currentUser)
                                }
                            },
                            onCloseCounselling = { notes ->
                                onSaveGuest(guest.copy(counsellingCompleted = true, counsellingNotes = notes))
                            },
                            onOpenSeniority = { seniorityGuest = guest },
                            onRefundSeniority = { refundSeniorityGuest = guest },
                            onOpenFollowUp = { followUpGuest = guest },
                            onOpenClosing = { closingGuest = guest },
                            onDelete = { onDeleteGuest(guest.id) },
                            canDelete = currentUser.role == "ADMIN"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Add Candidate FAB
            FloatingActionButton(
                onClick = { showManualAddGuest = true },
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_candidate_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Candidate")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Candidate", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Process File & ₹150 Dialog (Req 6)
    processFileGuest?.let { g ->
        ProcessFileDialog(
            guest = g,
            currentStaffName = currentUser.fullName,
            onDismiss = { processFileGuest = null },
            onSave = { hasArrived, fileProcessed, fileProcessedBy, regPaid, mode, ref ->
                val timeNow = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
                val updated = g.copy(
                    hasArrived = hasArrived,
                    fileProcessed = fileProcessed,
                    fileProcessedBy = fileProcessedBy,
                    registrationFeePaid = regPaid,
                    registrationPaymentMode = mode,
                    registrationTxnRef = ref,
                    registrationTimestamp = if (regPaid) timeNow else "",
                    arrivalTime = if (hasArrived && g.arrivalTime.isBlank()) timeNow else g.arrivalTime
                )
                onSaveGuest(updated)
                processFileGuest = null
            }
        )
    }

    // Registration ₹150 Refund Dialog
    refundRegGuest?.let { g ->
        RegistrationRefundDialog(
            guest = g,
            onDismiss = { refundRegGuest = null },
            onConfirmRefund = { reason ->
                onRefundRegistration?.invoke(g, reason)
                refundRegGuest = null
            }
        )
    }

    // Assign Counsellor Dialog (Enforces 7 Limit)
    assignCounsellorGuest?.let { g ->
        AssignCounsellorDialog(
            guest = g,
            distributors = if (allDistributors.isNotEmpty()) allDistributors else listOf(currentUser),
            counsellingCountMap = counsellingCountMap,
            onDismiss = { assignCounsellorGuest = null },
            onSelectDistributor = { dist ->
                onAttemptStartCounselling(g, dist)
                assignCounsellorGuest = null
            }
        )
    }

    // Seniority Dialog (Req 8 - Multi-stage & Voucher)
    seniorityGuest?.let { g ->
        SeniorityDialog(
            guest = g,
            onDismiss = { seniorityGuest = null },
            onSave = { amt, mode, ref, notes, voucherCollected, voucherNo ->
                if (onAddSeniorityPayment != null) {
                    onAddSeniorityPayment(g, amt, mode, ref, notes, voucherCollected, voucherNo)
                } else {
                    onCompleteSeniority(g, amt, mode, ref)
                }
                seniorityGuest = null
            },
            onRequestRefund = {
                val candidate = seniorityGuest
                seniorityGuest = null
                refundSeniorityGuest = candidate
            }
        )
    }

    // Seniority Refund Dialog
    refundSeniorityGuest?.let { g ->
        SeniorityRefundDialog(
            guest = g,
            onDismiss = { refundSeniorityGuest = null },
            onConfirmRefund = { amt, reason ->
                onRefundSeniority?.invoke(g, amt, reason)
                refundSeniorityGuest = null
            }
        )
    }

    // L3 Follow-up Dialog (Req 9 - Outcome & Reason)
    followUpGuest?.let { g ->
        FollowUpDialog(
            guest = g,
            onDismiss = { followUpGuest = null },
            onSave = { notes, outcome, notSaleReason ->
                if (onAddFollowUpWithOutcome != null) {
                    onAddFollowUpWithOutcome(g, notes, outcome, notSaleReason)
                } else {
                    onAddFollowUp(g, notes)
                }
                followUpGuest = null
            }
        )
    }

    // Sales Closing Dialog (Req 10)
    closingGuest?.let { g ->
        SalesClosingDialog(
            guest = g,
            onDismiss = { closingGuest = null },
            onSave = { totalAmt, mode, ref, notes ->
                onCompleteSalesClosing(g, totalAmt, mode, ref, notes)
                closingGuest = null
            }
        )
    }

    // Manual Add Guest Dialog
    if (showManualAddGuest) {
        ManualAddCandidateDialog(
            currentUser = currentUser,
            onDismiss = { showManualAddGuest = false },
            onSave = { newGuest ->
                onSaveGuest(newGuest)
                showManualAddGuest = false
            }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF2563EB) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFF475569)
        )
    }
}

@Composable
fun GuestPipelineCard(
    guest: GuestPipelineEntity,
    currentUser: UserEntity,
    counsellingCountMap: Map<String, Int> = emptyMap(),
    onProcessFile: () -> Unit,
    onRefundRegistration: () -> Unit,
    onStartCounselling: () -> Unit,
    onCloseCounselling: (notes: String) -> Unit,
    onOpenSeniority: () -> Unit,
    onRefundSeniority: () -> Unit,
    onOpenFollowUp: () -> Unit,
    onOpenClosing: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var showCounsellingCloseDialog by remember { mutableStateOf(false) }

    val payments = remember(guest.seniorityPaymentsJson) {
        parseSeniorityPaymentsJson(guest.seniorityPaymentsJson)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (guest.salesClosed) Color(0xFFF0FDF4) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = guest.guestName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        if (guest.salesClosed) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🎉 Closed", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                        } else if (guest.finalOutcome == "NOT_SALE") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lost (Not Sale)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                    Text(
                        text = "Phone: ${guest.phoneNumber} • Sponsor: ${guest.distributorName}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                if (guest.temporaryId.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = guest.temporaryId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step 6 Display: Office Arrival & File Process (₹150)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Step 1: Arrival & ₹150 File Entry", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (guest.registrationFeePaid && !guest.registrationRefunded) {
                                TextButton(
                                    onClick = onRefundRegistration,
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626)),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Refund ₹150", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Button(
                                onClick = onProcessFile,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (guest.registrationFeePaid) Color(0xFF059669) else Color(0xFF2563EB)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (guest.registrationFeePaid) "Edit ₹150 Entry" else "Process File & ₹150", fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Arrival: ${if (guest.hasArrived) "Arrived ✅ (${guest.arrivalTime})" else "Scheduled on ${guest.scheduledVisitDate}"}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "File Processed By: ${if (guest.fileProcessedBy.isNotBlank()) guest.fileProcessedBy else "Pending"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (guest.fileProcessedBy.isNotBlank()) Color(0xFF0F172A) else Color(0xFFDC2626)
                    )
                    Text(
                        text = "₹150 Fee: ${if (guest.registrationFeePaid) "Paid via ${guest.registrationPaymentMode} (Ref: ${guest.registrationTxnRef.ifBlank { "N/A" }})" else "Not Paid ❌"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (guest.registrationFeePaid) Color(0xFF059669) else Color(0xFFDC2626)
                    )
                    if (guest.registrationRefunded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚠️ ₹150 REFUNDED on ${guest.registrationRefundTimestamp} • Reason: ${guest.registrationRefundReason}",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step 7 Display: L2 Counselling (Unlocked after ₹150 paid)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Step 2: L2 Counselling (Limit: Max 7)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))

                        if (!guest.registrationFeePaid) {
                            Text("Locked (Requires ₹150)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        } else if (!guest.counsellingStarted) {
                            Button(
                                onClick = onStartCounselling,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp).testTag("start_counselling_btn_${guest.id}")
                            ) {
                                Text("Assign / Start L2", fontSize = 11.sp)
                            }
                        } else if (!guest.counsellingCompleted) {
                            Button(
                                onClick = { showCounsellingCloseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Complete & Notes", fontSize = 11.sp)
                            }
                        } else {
                            Text("Completed ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        }
                    }

                    if (guest.counsellingStarted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val currentLoad = if (guest.counsellorId.isNotBlank()) counsellingCountMap[guest.counsellorId] ?: 0 else 0
                        Text(
                            text = "Counsellor: ${guest.counsellorName} (Load: $currentLoad/7) • Started: ${guest.counsellingStartTime}",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                        if (guest.counsellingNotes.isNotBlank()) {
                            Text(
                                text = "Closing Notes: ${guest.counsellingNotes}",
                                fontSize = 11.sp,
                                color = Color(0xFF451A03),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Seniority Info Summary
            if (guest.seniorityAmount > 0 || guest.seniorityRefunded) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Seniority Total: ₹${guest.seniorityAmount.toInt()} (${payments.size} payment${if (payments.size > 1) "s" else ""})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                            if (guest.seniorityAmount > 0 && !guest.seniorityRefunded) {
                                TextButton(
                                    onClick = onRefundSeniority,
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626)),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Refund Seniority", fontSize = 11.sp)
                                }
                            }
                        }
                        if (guest.seniorityVoucherCollected) {
                            Text(
                                text = "🎟️ Voucher Issued: ${guest.seniorityVoucherNumber.ifBlank { "Collected" }}",
                                fontSize = 11.sp,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (guest.seniorityRefunded) {
                            Text(
                                text = "⚠️ Seniority REFUNDED: ₹${guest.seniorityRefundAmount.toInt()} on ${guest.seniorityRefundDateTime} (Reason: ${guest.seniorityRefundReason})",
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Follow-up Info Summary
            if (guest.followUpCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                val outcomeColor = when (guest.finalOutcome) {
                    "SALE" -> Color(0xFF047857)
                    "NOT_SALE" -> Color(0xFFDC2626)
                    else -> Color(0xFF475569)
                }
                Text(
                    text = "Follow-ups: ${guest.followUpCount}/3 • Status: ${
                        when (guest.finalOutcome) {
                            "SALE" -> "Agreed / Ready for Sales Close ✅"
                            "NOT_SALE" -> "Declined / Not Sale ❌ (${guest.notSaleReason})"
                            else -> "In Progress (Calls ongoing)"
                        }
                    }",
                    fontSize = 11.sp,
                    color = outcomeColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step 8 & 9 & 10 Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step 8: Seniority (can add more installments or view)
                OutlinedButton(
                    onClick = onOpenSeniority,
                    enabled = guest.counsellingCompleted,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        if (guest.seniorityCompleted) "Seniority (₹${guest.seniorityAmount.toInt()})" else "Seniority",
                        fontSize = 11.sp
                    )
                }

                // Step 9: L3 Follow-up (Max 3 times, not allowed if Not Sale or Closed)
                OutlinedButton(
                    onClick = onOpenFollowUp,
                    enabled = guest.seniorityCompleted && guest.followUpCount < 3 && !guest.salesClosed && guest.finalOutcome != "NOT_SALE",
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text("Follow-up (${guest.followUpCount}/3)", fontSize = 11.sp)
                }

                // Step 10: Sales Closing
                Button(
                    onClick = onOpenClosing,
                    enabled = guest.seniorityCompleted && !guest.salesClosed && guest.finalOutcome != "NOT_SALE",
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("sales_closing_btn_${guest.id}")
                ) {
                    Text(if (guest.salesClosed) "Closed 🎉" else "Sales Close", fontSize = 11.sp)
                }
            }

            if (canDelete) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Entry", color = Color(0xFFEF4444), fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Counselling Notes Dialog
    if (showCounsellingCloseDialog) {
        var closingNotes by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showCounsellingCloseDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.padding(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("L2 Counselling Closing Notes*", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Enter notes about the attended candidate before moving to Seniority:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = closingNotes,
                        onValueChange = { closingNotes = it },
                        label = { Text("Candidate Assessment / Notes") },
                        placeholder = { Text("e.g. Enthusiastic candidate, agreed for seniority payment") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showCounsellingCloseDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (closingNotes.isNotBlank()) {
                                    onCloseCounselling(closingNotes.trim())
                                    showCounsellingCloseDialog = false
                                }
                            }
                        ) {
                            Text("Confirm & Close L2")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualAddCandidateDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onSave: (GuestPipelineEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var scheduledDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add New Candidate to Pipeline", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                Text("Enters directly into Smart Group recruitment pipeline", fontSize = 12.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Candidate / Guest Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number*") },
                    placeholder = { Text("+91 98765 43210") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = scheduledDate,
                    onValueChange = { scheduledDate = it },
                    label = { Text("Office Visit Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg ?: "", color = Color(0xFFDC2626), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                errorMsg = "Candidate name and phone number are required."
                            } else {
                                onSave(
                                    GuestPipelineEntity(
                                        guestName = name.trim(),
                                        phoneNumber = phone.trim(),
                                        distributorId = currentUser.id,
                                        distributorName = currentUser.fullName,
                                        teamId = currentUser.teamId,
                                        scheduledVisitDate = scheduledDate.trim()
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Add Candidate")
                    }
                }
            }
        }
    }
}
