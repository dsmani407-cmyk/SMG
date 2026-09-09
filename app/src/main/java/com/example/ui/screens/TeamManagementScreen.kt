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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
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
import com.example.data.model.UserEntity

@Composable
fun TeamManagementScreen(
    currentUser: UserEntity,
    users: List<UserEntity>,
    onAddUser: (UserEntity) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Requirement 11:
    // Team Leader sees only distributors under their team.
    // Admin sees ALL team leaders and all distributors across all teams.
    val visibleUsers = if (currentUser.role == "ADMIN") {
        users
    } else {
        users.filter { it.teamId == currentUser.teamId }
    }

    val filteredUsers = visibleUsers.filter { u ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.lowercase()
            u.fullName.lowercase().contains(q) ||
            u.username.lowercase().contains(q) ||
            u.teamName.lowercase().contains(q) ||
            u.role.lowercase().contains(q) ||
            u.phone.contains(q)
        }
    }

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
                        text = if (currentUser.role == "ADMIN") "Smart Group Roster & Team Roster" else "My Team Distributors",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (currentUser.role == "ADMIN")
                            "Admin view: Full oversight of all team leaders, distributors, and credentials"
                        else
                            "Team Leader view: Add and manage distributors under ${currentUser.teamName}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search distributors by name, login ID, team...") },
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
                            .testTag("team_search_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Summary Pill
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (currentUser.role == "ADMIN") "Total Network Members" else "My Team Members",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${filteredUsers.count { it.role == "DISTRIBUTOR" }} Distributors • ${filteredUsers.count { it.role == "TEAM_LEADER" }} Team Leaders",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${filteredUsers.size}",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // User Cards
                items(filteredUsers) { u ->
                    UserRosterCard(
                        user = u,
                        currentLoggedInUser = currentUser,
                        onDelete = { onDeleteUser(u.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Add Distributor FAB (Team Leader & Admin can add as many distributors as needed!)
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_distributor_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Distributor")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Distributor", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddDialog) {
        AddDistributorDialog(
            currentUser = currentUser,
            onDismiss = { showAddDialog = false },
            onSave = { newUser ->
                onAddUser(newUser)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun UserRosterCard(
    user: UserEntity,
    currentLoggedInUser: UserEntity,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (avatarBg, roleIcon, roleColor) = when (user.role) {
                        "ADMIN" -> Triple(Color(0xFFFEE2E2), Icons.Default.Key, Color(0xFFDC2626))
                        "TEAM_LEADER" -> Triple(Color(0xFFDBEAFE), Icons.Default.SupervisorAccount, Color(0xFF2563EB))
                        else -> Triple(Color(0xFFDCFCE7), Icons.Default.Person, Color(0xFF059669))
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(avatarBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = roleIcon, contentDescription = null, tint = roleColor, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Login ID: ${user.username} • ${user.teamName}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = user.role.replace('_', ' '),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (user.phone.isNotBlank()) {
                        Text(text = "Phone: ${user.phone}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    // Admin can see password
                    if (currentLoggedInUser.role == "ADMIN") {
                        Text(text = "Password: ${user.password}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                    }
                }

                if (currentLoggedInUser.role == "ADMIN" && user.role != "ADMIN") {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete User", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddDistributorDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf(currentUser.teamName) }
    var teamId by remember { mutableStateOf(currentUser.teamId) }
    var role by remember { mutableStateOf("DISTRIBUTOR") }
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
                        text = "Add New Distributor / Member",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                Text(
                    text = "Distributor will use individual login ID & password to sign in",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMsg = null },
                    label = { Text("Full Name*") },
                    placeholder = { Text("e.g. Ramesh Kumar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMsg = null },
                    label = { Text("Unique Login ID / Username*") },
                    placeholder = { Text("e.g. ramesh_sg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = null },
                    label = { Text("Login Password*") },
                    placeholder = { Text("Set a secure password") },
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

                if (currentUser.role == "ADMIN") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Assigned Team Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                            if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                                errorMsg = "Full name, login ID, and password are required."
                            } else {
                                val newId = "usr_${System.currentTimeMillis()}"
                                onSave(
                                    UserEntity(
                                        id = newId,
                                        username = username.trim().lowercase(),
                                        password = password.trim(),
                                        fullName = fullName.trim(),
                                        role = role,
                                        teamId = teamId,
                                        teamName = teamName.trim(),
                                        phone = phone.trim(),
                                        email = email.trim()
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Create Member")
                    }
                }
            }
        }
    }
}
}
