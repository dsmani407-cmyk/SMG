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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AdPostEntity
import com.example.data.model.UserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdPostingScreen(
    currentUser: UserEntity,
    todayDate: String,
    adPosts: List<AdPostEntity>,
    onSavePost: (AdPostEntity) -> Unit,
    onDeletePost: (Long) -> Unit
) {
    // Current user's ads for today
    val myAdPosts = adPosts.filter { it.userId == currentUser.id && it.date == todayDate }
        .sortedBy { it.sourceIndex }
    val completedCount = myAdPosts.count { it.isPosted }

    var editingPost by remember { mutableStateOf<AdPostEntity?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily 5 Source Ad Posting",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Log and verify your daily advertisement releases across 5 media sources",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF059669).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Today's Ad Coverage ($todayDate)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (completedCount >= 5) "All 5 sources completed! Great job!" else "Need ${5 - completedCount} more source post(s)",
                                        color = if (completedCount >= 5) Color(0xFF34D399) else Color(0xFFFCD34D),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = "$completedCount / 5",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val progress = (completedCount.toFloat() / 5f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF334155),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5 Ad Slots
            items(myAdPosts) { post ->
                AdSourceSlotCard(
                    post = post,
                    onTogglePosted = { isChecked ->
                        val timeNow = if (isChecked) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) else ""
                        onSavePost(post.copy(isPosted = isChecked, postTime = timeNow))
                    },
                    onEditDetails = {
                        editingPost = post
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Other Team Members' Posts (Admin / TL)
            if (currentUser.role == "ADMIN" || currentUser.role == "TEAM_LEADER") {
                val otherPosts = adPosts.filter { it.userId != currentUser.id }
                if (otherPosts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (currentUser.role == "ADMIN") "All Distributors Ad Submissions" else "Team Members Ad Submissions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(otherPosts) { post ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
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
                                    Text(
                                        text = "${post.userName} • Source #${post.sourceIndex}: ${post.sourceName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Status: ${if (post.isPosted) "Posted at ${post.postTime}" else "Not posted"} • Details: ${if (post.postDetails.isNotBlank()) post.postDetails else "None"}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                if (currentUser.role == "ADMIN") {
                                    IconButton(onClick = { onDeletePost(post.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    // Edit Ad Source Dialog
    editingPost?.let { post ->
        EditAdPostDialog(
            post = post,
            onDismiss = { editingPost = null },
            onSave = { updated ->
                onSavePost(updated)
                editingPost = null
            }
        )
    }
}

@Composable
fun AdSourceSlotCard(
    post: AdPostEntity,
    onTogglePosted: (Boolean) -> Unit,
    onEditDetails: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (post.isPosted) Color(0xFFF0FDF4) else Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = post.isPosted,
                onCheckedChange = onTogglePosted,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF059669))
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Source ${post.sourceIndex}: ${post.sourceName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (post.isPosted) Color(0xFF065F46) else Color(0xFF1E293B)
                    )
                }

                if (post.isPosted && post.postTime.isNotBlank()) {
                    Text(
                        text = "Posted at: ${post.postTime}",
                        fontSize = 11.sp,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (post.postDetails.isNotBlank()) {
                    Text(
                        text = "Ref: ${post.postDetails}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                } else {
                    Text(
                        text = "Tap pencil to add link, headline, or screenshot notes",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            IconButton(onClick = onEditDetails) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit details",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditAdPostDialog(
    post: AdPostEntity,
    onDismiss: () -> Unit,
    onSave: (AdPostEntity) -> Unit
) {
    var sourceName by remember { mutableStateOf(post.sourceName) }
    var details by remember { mutableStateOf(post.postDetails) }
    var isPosted by remember { mutableStateOf(post.isPosted) }
    var postTime by remember {
        mutableStateOf(
            if (post.postTime.isNotBlank()) post.postTime
            else SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
    }

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
                    text = "Update Ad Source #${post.sourceIndex}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Specify which platform and details for this ad posting",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = sourceName,
                    onValueChange = { sourceName = it },
                    label = { Text("Ad Source Name*") },
                    placeholder = { Text("e.g. WhatsApp, Instagram, Facebook, LinkedIn") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Post Link / Ad Headline / Group Name") },
                    placeholder = { Text("e.g. Posted in Chennai Job Seekers FB Group") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPosted,
                        onCheckedChange = { isPosted = it }
                    )
                    Text("Mark as Successfully Posted Today", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                if (isPosted) {
                    OutlinedTextField(
                        value = postTime,
                        onValueChange = { postTime = it },
                        label = { Text("Posting Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onSave(
                                post.copy(
                                    sourceName = sourceName.trim(),
                                    postDetails = details.trim(),
                                    isPosted = isPosted,
                                    postTime = if (isPosted) postTime.trim() else ""
                                )
                            )
                        }
                    ) {
                        Text("Save Slot")
                    }
                }
            }
        }
    }
}
}
