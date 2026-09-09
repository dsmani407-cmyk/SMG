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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdPostEntity
import com.example.data.model.CallLogEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.RoyalBluePrimary
import com.example.ui.theme.RoyalBlueSoft
import com.example.ui.theme.SlateBorder

@Composable
fun DistributorHomeScreen(
    currentUser: UserEntity,
    todayTask: DailyTaskEntity?,
    adPosts: List<AdPostEntity>,
    callLogs: List<CallLogEntity>,
    guests: List<GuestPipelineEntity>,
    onNavigate: (String) -> Unit
) {
    val postedAdsCount = adPosts.count { it.isPosted }
    val loggedCallsCount = callLogs.count { it.contactName.isNotBlank() }
    val confirmedCallsCount = callLogs.count { it.isConfirmed }
    val closedSalesCount = guests.count { it.salesClosed }
    val totalSalesAmount = guests.filter { it.salesClosed }.sumOf { it.totalPackageAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy800),
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
                            text = "Welcome back,",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = currentUser.fullName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Team: ${currentUser.teamName}",
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoyalBluePrimary)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ID: ${currentUser.username.uppercase()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Evening checkout status banner with Thumbs Up / Thumbs Down indicator
                val eveningStatus = todayTask?.eveningCheckoutStatus ?: "PENDING"
                val isCompleted = eveningStatus == "COMPLETED"
                val statusBg = if (isCompleted) EmeraldSuccess else Color(0xFFE11D48)

                Surface(
                    color = statusBg.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, statusBg, RoundedCornerShape(10.dp))
                        .clickable { onNavigate("TASKS") }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                                contentDescription = null,
                                tint = if (isCompleted) Color(0xFF4ADE80) else Color(0xFFFB7185),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isCompleted) "Evening Closing: THUMBS UP 👍" else "Evening Closing: THUMBS DOWN 👎",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                if (isCompleted && todayTask?.eveningCheckoutTime?.isNotBlank() == true) {
                                    Text(
                                        text = "All tasks complete • Checked out at ${todayTask.eveningCheckoutTime}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF86EFAC)
                                    )
                                } else {
                                    Text(
                                        text = "Incomplete targets pending • Tap to edit and complete daily checkout",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFCA5A5)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Progress Summary Metrics (5 Ads & 20 Calls)
        Text(
            text = "Today's Key Targets",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Navy900
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // 5 Ad Sources Metric Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                    .clickable { onNavigate("ADS") }
                    .testTag("home_ads_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RoyalBlueSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = RoyalBluePrimary, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "$postedAdsCount / 5",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (postedAdsCount >= 5) EmeraldSuccess else RoyalBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "5 Ad Sources", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                    Text(
                        text = if (postedAdsCount >= 5) "All 5 Posted ✓" else "${5 - postedAdsCount} remaining",
                        fontSize = 11.sp,
                        color = if (postedAdsCount >= 5) EmeraldSuccess else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (postedAdsCount / 5f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (postedAdsCount >= 5) EmeraldSuccess else RoyalBluePrimary,
                        trackColor = SlateBorder
                    )
                }
            }

            // 20 Calls Metric Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                    .clickable { onNavigate("CALLS") }
                    .testTag("home_calls_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "$loggedCallsCount / 20",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (loggedCallsCount >= 20) EmeraldSuccess else GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "20 Daily Calls", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Navy900)
                    Text(
                        text = "$confirmedCallsCount visits confirmed",
                        fontSize = 11.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (loggedCallsCount / 20f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (loggedCallsCount >= 20) EmeraldSuccess else GoldAccent,
                        trackColor = SlateBorder
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sales & Funnel Performance Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSoft),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EmeraldSuccess.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable { onNavigate("PIPELINE") }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sales Closings: $closedSalesCount candidates",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Revenue Closed: ₹$totalSalesAmount",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Navy900
                    )
                    Text(
                        text = "Tap to manage ₹150 fees, L2 Counselling, Seniority & Closings",
                        fontSize = 11.sp,
                        color = Navy800
                    )
                }

                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = EmeraldSuccess)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Action Navigation Tiles
        Text(
            text = "Workflow & Actions",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Navy900
        )
        Spacer(modifier = Modifier.height(8.dp))

        QuickActionTile(
            title = "1. Daily Tasks & Evening Status",
            subtitle = "Update checklist & evening checkout completion",
            icon = Icons.Default.ListAlt,
            iconTint = RoyalBluePrimary,
            onClick = { onNavigate("TASKS") }
        )

        QuickActionTile(
            title = "2. 5 Ad Sources Posting",
            subtitle = "Track WhatsApp, Instagram, Facebook, Telegram & Job Portals",
            icon = Icons.Default.Campaign,
            iconTint = GoldAccent,
            onClick = { onNavigate("ADS") }
        )

        QuickActionTile(
            title = "3. 20 Prospect Calls & Scheduling",
            subtitle = "Log call responses & schedule office visit dates",
            icon = Icons.Default.PhoneInTalk,
            iconTint = RoyalBlueLight,
            onClick = { onNavigate("CALLS") }
        )

        QuickActionTile(
            title = "4. Guest Funnel, L2 & Sales Closing",
            subtitle = "₹150 Registration, L2 limit (Max 7), Seniority Temp ID & Sales",
            icon = Icons.Default.MonetizationOn,
            iconTint = EmeraldSuccess,
            onClick = { onNavigate("PIPELINE") }
        )

        if (currentUser.role == UserRole.TEAM_LEADER.name || currentUser.role == UserRole.ADMIN.name) {
            QuickActionTile(
                title = "5. Team Management",
                subtitle = "Manage distributors & team performance",
                icon = Icons.Default.Group,
                iconTint = GoldAccent,
                onClick = { onNavigate("TEAM") }
            )
        }

        if (currentUser.role == UserRole.ADMIN.name) {
            QuickActionTile(
                title = "6. Admin Master Panel",
                subtitle = "Super control: All teams, full audit logs & CRUD control",
                icon = Icons.Default.Star,
                iconTint = RoseError,
                onClick = { onNavigate("ADMIN") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}
