package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserEntity

@Composable
fun AppTopBar(
    currentUser: UserEntity?,
    isCloudSyncing: Boolean,
    onSyncClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_smart_groups_logo),
                            contentDescription = "Smart Groups Logo",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Smart Group",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (currentUser != null) "${currentUser.teamName} • ${currentUser.role.replace('_', ' ')}" else "Enterprise Portal",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Cloud Sync Pill & Logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Cloud Sync Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { onSyncClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCloudSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Synced",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cloud Active", color = Color(0xFFE2E8F0), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // User Profile / Logout
                    if (currentUser != null) {
                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                                .testTag("app_logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Log Out",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class NavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun AppBottomNav(
    userRole: String,
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = when (userRole) {
        "ADMIN" -> listOf(
            NavItem("admin", "Master View", Icons.Default.AdminPanelSettings),
            NavItem("pipeline", "Pipeline", Icons.Default.FilterAlt),
            NavItem("team", "Teams & Users", Icons.Default.Group),
            NavItem("cloud", "Cloud Audit", Icons.Default.CloudSync)
        )
        "TEAM_LEADER" -> listOf(
            NavItem("dashboard", "Overview", Icons.Default.Dashboard),
            NavItem("team", "My Team", Icons.Default.Group),
            NavItem("calls", "Team Calls", Icons.Default.PhoneInTalk),
            NavItem("pipeline", "Pipeline", Icons.Default.FilterAlt),
            NavItem("tasks", "Tasks", Icons.Default.Checklist)
        )
        else -> listOf(
            NavItem("dashboard", "Home", Icons.Default.Dashboard),
            NavItem("tasks", "Daily Tasks", Icons.Default.Checklist),
            NavItem("ads", "5 Ad Sources", Icons.Default.Campaign),
            NavItem("calls", "20 Calls", Icons.Default.PhoneInTalk),
            NavItem("pipeline", "Pipeline", Icons.Default.FilterAlt)
        )
    }

    Surface(
        color = Color(0xFF0F172A),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color(0xFF0F172A),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            items.forEach { item ->
                val selected = currentTab == item.id
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(item.id) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF2563EB),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_${item.id}")
                )
            }
        }
    }
}
