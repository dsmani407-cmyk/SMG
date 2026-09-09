package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.SeniorityPaymentItem
import com.example.data.model.UserEntity
import com.example.data.model.parseSeniorityPaymentsJson
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.sin

// ---------------------------------------------------------------------------------
// 1. Counselling Limit Reached Alert Pop-up (Requirement 7)
// ---------------------------------------------------------------------------------
@Composable
fun CounsellingLimitAlertDialog(
    distributorName: String = "",
    message: String = "",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFFEF2F2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert Limit",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        title = {
            Text(
                text = "Counselling Limit Reached!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No 8th Counselling (L2) Permitted!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C)
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (distributorName.isNotBlank()) {
                            Text(
                                text = "Distributor: $distributorName",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        Text(
                            text = if (message.isNotBlank()) message else "A single distributor cannot conduct counselling (L2) for more than 7 persons. This distributor has already reached the maximum limit of 7 candidates.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Action: Please assign another active distributor or consult your Team Leader / Admin.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dismiss_counselling_alert_btn")
            ) {
                Text("Understood (Dismiss)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ---------------------------------------------------------------------------------
// 2. Congratulations Sales Closing Pop-up with Confetti Animation (Requirement 10)
// ---------------------------------------------------------------------------------
@Composable
fun ConfettiCanvas(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    val confettiColors = listOf(
        Color(0xFFE11D48),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFFFBBF24),
        Color(0xFF06B6D4)
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        for (i in 0 until 35) {
            val color = confettiColors[i % confettiColors.size]
            val xBase = ((i * 37) % 100) / 100f * w
            val speed = 0.6f + ((i * 23) % 40) / 100f
            val yPos = (progress * speed * h + (i * 29) % h) % h
            val xWiggle = sin((progress * 6 + i).toDouble()).toFloat() * 18f
            val particleW = 8.dp.toPx()
            val particleH = 14.dp.toPx()

            drawRect(
                color = color,
                topLeft = Offset((xBase + xWiggle).coerceIn(0f, w - particleW), yPos),
                size = Size(particleW, particleH)
            )
        }
    }
}

@Composable
fun CongratulationsSalesDialog(
    guestName: String = "",
    totalAmount: Double = 0.0,
    guest: GuestPipelineEntity? = null,
    distributorName: String = "",
    onDismiss: () -> Unit
) {
    val finalGuestName = guest?.guestName ?: guestName
    val finalAmount = guest?.totalPackageAmount ?: totalAmount
    val finalDistributor = if (distributorName.isNotBlank()) distributorName else (guest?.distributorName ?: "")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 20.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .wrapContentHeight()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Confetti animation background layer
                    ConfettiCanvas(
                        modifier = Modifier
                            .matchParentSize()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Trophy / Celebration badge
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Congratulations",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "🎉 CONGRATULATIONS! 🎉",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF047857)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Sales Closing Successfully Completed!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4).copy(alpha = 0.95f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Candidate Name",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = finalGuestName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                if (finalDistributor.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Distributor: $finalDistributor",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF047857)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Closed Package Amount",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "₹${String.format(java.util.Locale.US, "%,.2f", finalAmount)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF047857)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Great job! The candidate is now successfully enrolled and recorded in Smart Group records.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("congratulations_continue_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Celebration, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Awesome! Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 3. Forgot Password Dialog (Requirement 12 & Firebase sendPasswordResetEmail)
// ---------------------------------------------------------------------------------
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (identifier: String, newPass: String) -> Unit,
    onSendFirebaseResetEmail: ((emailOrIdentifier: String, onResult: (Boolean, String) -> Unit) -> Unit)? = null
) {
    var recoveryMode by remember { mutableStateOf("FIREBASE_EMAIL") } // "FIREBASE_EMAIL" or "IN_APP_OTP"
    var identifier by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // for OTP mode
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSendingEmail by remember { mutableStateOf(false) }
    var emailSentSuccessMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (emailSentSuccessMessage != null) "Reset Email Dispatched" else "Recover Account",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (emailSentSuccessMessage != null) {
                    // Success View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Password Reset Link Sent!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = emailSentSuccessMessage ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    // Mode Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (recoveryMode == "FIREBASE_EMAIL") Color(0xFF2563EB) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    recoveryMode = "FIREBASE_EMAIL"
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = "📧 Firebase Email",
                                color = if (recoveryMode == "FIREBASE_EMAIL") Color.White else Color(0xFF475569),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (recoveryMode == "IN_APP_OTP") Color(0xFF2563EB) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    recoveryMode = "IN_APP_OTP"
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = "📱 Instant OTP",
                                color = if (recoveryMode == "IN_APP_OTP") Color.White else Color(0xFF475569),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (recoveryMode == "FIREBASE_EMAIL") {
                        Text(
                            text = "Enter your registered email address or login ID. Firebase Auth will send a secure password reset link to your email inbox.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = {
                                identifier = it
                                errorMessage = null
                            },
                            label = { Text("Registered Email or Login ID") },
                            placeholder = { Text("e.g. d.s.mani407@gmail.com or mani") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_pass_identifier_input")
                        )
                    } else {
                        // In-App OTP Mode
                        if (step == 1) {
                            Text(
                                text = "Enter your registered Mobile Number or Email ID to receive an instant verification code.",
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = identifier,
                                onValueChange = {
                                    identifier = it
                                    errorMessage = null
                                },
                                label = { Text("Registered Phone or Email") },
                                placeholder = { Text("e.g. 9840556677 or mani") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forgot_pass_otp_identifier_input")
                            )
                        } else {
                            Text(
                                text = "Verification code sent to $identifier. Set your new password.",
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text("Enter OTP Code") },
                                placeholder = { Text("123456") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm New Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (emailSentSuccessMessage != null) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.testTag("dismiss_success_reset_btn")
                ) {
                    Text("Done (Back to Login)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else if (recoveryMode == "FIREBASE_EMAIL") {
                Button(
                    onClick = {
                        if (identifier.isBlank()) {
                            errorMessage = "Please enter your registered email address or login ID."
                        } else {
                            isSendingEmail = true
                            errorMessage = null
                            if (onSendFirebaseResetEmail != null) {
                                onSendFirebaseResetEmail(identifier) { success, resultMsg ->
                                    isSendingEmail = false
                                    if (success) {
                                        emailSentSuccessMessage = resultMsg
                                    } else {
                                        errorMessage = resultMsg
                                    }
                                }
                            } else {
                                isSendingEmail = false
                                onSubmit(identifier, "firebase_email_reset")
                            }
                        }
                    },
                    enabled = !isSendingEmail,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.testTag("forgot_pass_action_btn")
                ) {
                    if (isSendingEmail) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sending...")
                    } else {
                        Text("Send Reset Link (Firebase Auth)")
                    }
                }
            } else {
                // In-App OTP confirmation
                Button(
                    onClick = {
                        if (step == 1) {
                            if (identifier.isBlank()) {
                                errorMessage = "Please enter your registered phone or email."
                            } else {
                                step = 2
                                otpCode = "123456"
                            }
                        } else {
                            if (newPassword.isBlank() || newPassword.length < 4) {
                                errorMessage = "Password must be at least 4 characters."
                            } else if (newPassword != confirmPassword) {
                                errorMessage = "Passwords do not match."
                            } else {
                                onSubmit(identifier, newPassword)
                            }
                        }
                    },
                    modifier = Modifier.testTag("forgot_pass_action_btn")
                ) {
                    Text(if (step == 1) "Send OTP" else "Update Password")
                }
            }
        },
        dismissButton = {
            if (emailSentSuccessMessage == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// ---------------------------------------------------------------------------------
// 4. File Processing & ₹150 Registration Entry (Requirement 6)
// ---------------------------------------------------------------------------------
@Composable
fun ProcessFileDialog(
    guest: GuestPipelineEntity,
    currentStaffName: String,
    onDismiss: () -> Unit,
    onSave: (
        hasArrived: Boolean,
        fileProcessed: Boolean,
        fileProcessedBy: String,
        registrationPaid: Boolean,
        paymentMode: String,
        txnRef: String
    ) -> Unit
) {
    var hasArrived by remember { mutableStateOf(guest.hasArrived) }
    var fileProcessed by remember { mutableStateOf(guest.fileProcessed) }
    var fileProcessedBy by remember { mutableStateOf(if (guest.fileProcessedBy.isNotBlank()) guest.fileProcessedBy else currentStaffName) }
    var feePaid by remember { mutableStateOf(guest.registrationFeePaid) }
    var paymentMode by remember { mutableStateOf(if (guest.registrationPaymentMode != "NOT_PAID") guest.registrationPaymentMode else "HAND_CASH") }
    var txnRef by remember { mutableStateOf(guest.registrationTxnRef) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "File Process & ₹150 Fee",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Candidate: ${guest.guestName} (${guest.phoneNumber})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Office Arrival
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "1. Office Arrival Status",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = hasArrived,
                                onClick = { hasArrived = true }
                            )
                            Text("Arrived at Office", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = !hasArrived,
                                onClick = { hasArrived = false }
                            )
                            Text("Not Yet", fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Mandatory File Process Staff
                OutlinedTextField(
                    value = fileProcessedBy,
                    onValueChange = {
                        fileProcessedBy = it
                        errorMsg = null
                    },
                    label = { Text("File Processed By (Compulsory Staff Name)*") },
                    placeholder = { Text("Staff / Distributor Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_processed_by_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. ₹150 Registration Payment
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "₹150 Registration Fee Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = feePaid,
                                onClick = { feePaid = true }
                            )
                            Text("Paid ₹150", fontWeight = FontWeight.SemiBold, color = Color(0xFF047857))
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = !feePaid,
                                onClick = { feePaid = false }
                            )
                            Text("Not Paid", color = Color(0xFFDC2626))
                        }

                        if (feePaid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select Payment Mode:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = paymentMode == "HAND_CASH",
                                    onClick = { paymentMode = "HAND_CASH" }
                                )
                                Text("Hand Cash", fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = paymentMode == "BANK_TRANSFER",
                                    onClick = { paymentMode = "BANK_TRANSFER" }
                                )
                                Text("Bank Transfer", fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = paymentMode == "UPI_TRANSFER",
                                    onClick = { paymentMode = "UPI_TRANSFER" }
                                )
                                Text("UPI Transfer", fontSize = 13.sp)
                            }

                            if (paymentMode == "BANK_TRANSFER" || paymentMode == "UPI_TRANSFER") {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = txnRef,
                                    onValueChange = {
                                        txnRef = it
                                        errorMsg = null
                                    },
                                    label = { Text("Transaction Reference Number*") },
                                    placeholder = { Text("e.g. UPI/2026/89412 or UTR No.") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("txn_ref_input")
                                )
                            }
                        }
                    }
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (fileProcessedBy.isBlank()) {
                                errorMsg = "Compulsory: Please mention who processed the file."
                            } else if (feePaid && (paymentMode == "BANK_TRANSFER" || paymentMode == "UPI_TRANSFER") && txnRef.isBlank()) {
                                errorMsg = "Transaction Reference Number is required for Bank/UPI transfer."
                            } else {
                                onSave(
                                    hasArrived,
                                    true,
                                    fileProcessedBy.trim(),
                                    feePaid,
                                    if (feePaid) paymentMode else "NOT_PAID",
                                    if (feePaid) txnRef.trim() else ""
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_file_process_btn")
                    ) {
                        Text("Save & Update")
                    }
                }
            }
        }
    }
}
}

// ---------------------------------------------------------------------------------
// 4B. Registration Fee ₹150 Refund Dialog
// ---------------------------------------------------------------------------------
@Composable
fun RegistrationRefundDialog(
    guest: GuestPipelineEntity,
    onDismiss: () -> Unit,
    onConfirmRefund: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Refund ₹150 Registration Fee",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Candidate: ${guest.guestName} (${guest.phoneNumber})",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This will mark the ₹150 registration fee as REFUNDED. A mandatory reason is required for administrative tracking and team leader audit.",
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        errorMsg = null
                    },
                    label = { Text("Compulsory Refund Reason*") },
                    placeholder = { Text("e.g. Candidate not interested, duplicate payment") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_refund_reason_input")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg ?: "", color = Color(0xFFDC2626), fontSize = 12.sp)
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
                            if (reason.isBlank()) {
                                errorMsg = "Please enter the reason for refund."
                            } else {
                                onConfirmRefund(reason.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.testTag("confirm_reg_refund_btn")
                    ) {
                        Text("Confirm ₹150 Refund")
                    }
                }
            }
        }
    }
}
}

// ---------------------------------------------------------------------------------
// 5. Seniority Procedure, Multi-Stage Payment & Voucher Dialog (Requirement 8)
// ---------------------------------------------------------------------------------
@Composable
fun SeniorityDialog(
    guest: GuestPipelineEntity,
    onDismiss: () -> Unit,
    onSave: (amount: Double, paymentMode: String, txnRef: String, notes: String, voucherCollected: Boolean, voucherNumber: String) -> Unit,
    onRequestRefund: (() -> Unit)? = null
) {
    var amountText by remember { mutableStateOf("1000") }
    var paymentMode by remember { mutableStateOf("UPI_TRANSFER") }
    var txnRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var voucherCollected by remember { mutableStateOf(guest.seniorityVoucherCollected) }
    var voucherNumber by remember { mutableStateOf(guest.seniorityVoucherNumber) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val existingPayments = remember(guest.seniorityPaymentsJson) {
        parseSeniorityPaymentsJson(guest.seniorityPaymentsJson)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seniority Procedure & ID",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(
                    text = "Candidate: ${guest.guestName}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                if (guest.temporaryId.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Temporary ID: ${guest.temporaryId}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                // Existing cumulative seniority payments summary
                if (existingPayments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Paid Seniority Installments (${existingPayments.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            existingPayments.forEachIndexed { idx, p ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${idx + 1} (${p.paymentMode}): ₹${p.amount}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = p.timestamp.take(16),
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF86EFAC))
                            Text(
                                text = "Total Seniority Paid: ₹${guest.seniorityAmount}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (existingPayments.isEmpty()) "Add Seniority Payment:" else "Add Additional Seniority Installment:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Payment Mode:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMode == "HAND_CASH",
                        onClick = { paymentMode = "HAND_CASH" }
                    )
                    Text("Hand Cash", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMode == "BANK_TRANSFER",
                        onClick = { paymentMode = "BANK_TRANSFER" }
                    )
                    Text("Bank Transfer", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMode == "UPI_TRANSFER",
                        onClick = { paymentMode = "UPI_TRANSFER" }
                    )
                    Text("UPI Transfer", fontSize = 13.sp)
                }

                if (paymentMode == "BANK_TRANSFER" || paymentMode == "UPI_TRANSFER") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = txnRef,
                        onValueChange = {
                            txnRef = it
                            errorMsg = null
                        },
                        label = { Text("Transaction Reference Number*") },
                        placeholder = { Text("e.g. UTR / UPI Ref ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks / Notes (Optional)") },
                    placeholder = { Text("e.g. Part-payment advance, token fee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Voucher Collection
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = voucherCollected,
                                onCheckedChange = { voucherCollected = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                            )
                            Text("Seniority Voucher Collected / Issued", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (voucherCollected) {
                            OutlinedTextField(
                                value = voucherNumber,
                                onValueChange = { voucherNumber = it },
                                label = { Text("Voucher / Receipt Slip Number") },
                                placeholder = { Text("e.g. VCH-2026-081") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                if (guest.seniorityAmount > 0 && onRequestRefund != null && !guest.seniorityRefunded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = onRequestRefund,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                    ) {
                        Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Candidate Cancelling? Refund Seniority Amount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (guest.seniorityRefunded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "REFUNDED: ₹${guest.seniorityRefundAmount} on ${guest.seniorityRefundDateTime}",
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Reason: ${guest.seniorityRefundReason}",
                                color = Color(0xFF7F1D1D),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                errorMsg = "Please enter a valid payment amount."
                            } else if ((paymentMode == "BANK_TRANSFER" || paymentMode == "UPI_TRANSFER") && txnRef.isBlank()) {
                                errorMsg = "Transaction Reference Number is required."
                            } else {
                                onSave(amt, paymentMode, txnRef.trim(), notes.trim(), voucherCollected, voucherNumber.trim())
                            }
                        }
                    ) {
                        Text(if (guest.temporaryId.isBlank()) "Generate Temp ID" else "Add Payment")
                    }
                }
            }
        }
    }
}
}

// ---------------------------------------------------------------------------------
// 5B. Seniority Refund Dialog
// ---------------------------------------------------------------------------------
@Composable
fun SeniorityRefundDialog(
    guest: GuestPipelineEntity,
    onDismiss: () -> Unit,
    onConfirmRefund: (amount: Double, reason: String) -> Unit
) {
    var amountText by remember { mutableStateOf(guest.seniorityAmount.toString()) }
    var reason by remember { mutableStateOf("") }
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
                    text = "Refund Seniority Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
                Text(
                    text = "Candidate: ${guest.guestName} (${guest.temporaryId})",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Refund Amount (₹)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        errorMsg = null
                    },
                    label = { Text("Compulsory Cancellation Reason*") },
                    placeholder = { Text("Why is seniority being refunded?") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg ?: "", color = Color(0xFFDC2626), fontSize = 12.sp)
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
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                errorMsg = "Please enter a valid refund amount."
                            } else if (reason.isBlank()) {
                                errorMsg = "Cancellation reason is required."
                            } else {
                                onConfirmRefund(amt, reason.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Confirm Refund")
                    }
                }
            }
        }
    }
}
}

// ---------------------------------------------------------------------------------
// 6. L3 Sales Follow-up Dialog with Outcome & Not-Sale Reason (Requirement 9)
// ---------------------------------------------------------------------------------
@Composable
fun FollowUpDialog(
    guest: GuestPipelineEntity,
    onDismiss: () -> Unit,
    onSave: (notes: String, outcome: String, notSaleReason: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("PENDING") } // PENDING, SALE, NOT_SALE
    var notSaleReason by remember { mutableStateOf("") }
    var selectedPredefinedReason by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val nextFollowUpIndex = guest.followUpCount + 1

    val predefinedReasons = listOf(
        "Financial issue / cannot arrange funds",
        "Family / parent objection",
        "Not interested in direct selling",
        "Joined another company / competitor",
        "Relocation / distance / time constraint",
        "Other"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
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
                    text = "L3 Sales Follow-up (#$nextFollowUpIndex of 3)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Candidate: ${guest.guestName} (${guest.temporaryId})",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Previous Follow-ups history
                if (guest.followUp1Notes.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Follow-up 1 (${guest.followUp1Date}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(guest.followUp1Notes, fontSize = 12.sp, color = Color(0xFF334155))
                        }
                    }
                }
                if (guest.followUp2Notes.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Follow-up 2 (${guest.followUp2Date}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(guest.followUp2Notes, fontSize = 12.sp, color = Color(0xFF334155))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        errorMsg = null
                    },
                    label = { Text("Follow-up #$nextFollowUpIndex Conversation Notes*") },
                    placeholder = { Text("What did the candidate say during this follow-up call?") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Outcome Selection
                Text("Follow-up Result / Outcome:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = outcome == "PENDING",
                        onClick = { outcome = "PENDING" }
                    )
                    Text("In Progress (Next follow-up)", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = outcome == "SALE",
                        onClick = { outcome = "SALE" }
                    )
                    Text("Agreed! Ready for Sales Closing", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF047857))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = outcome == "NOT_SALE",
                        onClick = { outcome = "NOT_SALE" }
                    )
                    Text("Declined / Lost (Not Sale)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                }

                // If Not Sale, prompt for mandatory reason
                if (outcome == "NOT_SALE") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Select Reason for Not Sale (Compulsory):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            predefinedReasons.forEach { r ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedPredefinedReason = r }
                                ) {
                                    RadioButton(
                                        selected = selectedPredefinedReason == r,
                                        onClick = { selectedPredefinedReason = r }
                                    )
                                    Text(r, fontSize = 12.sp, color = Color(0xFF7F1D1D))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = notSaleReason,
                                onValueChange = { notSaleReason = it },
                                label = { Text("Detailed Not-Sale Reason / Feedback*") },
                                placeholder = { Text("Specific candidate objections, remarks, etc.") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Text(
                    text = "Rule: Exactly 3 day-by-day follow-ups permitted. Max 3 times only.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 6.dp)
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
                            if (notes.isBlank()) {
                                errorMsg = "Please enter conversation notes."
                            } else if (outcome == "NOT_SALE" && selectedPredefinedReason.isBlank() && notSaleReason.isBlank()) {
                                errorMsg = "Please select or describe the reason for Not Sale."
                            } else {
                                val fullReason = if (selectedPredefinedReason.isNotBlank() && notSaleReason.isNotBlank()) {
                                    "$selectedPredefinedReason: ${notSaleReason.trim()}"
                                } else {
                                    selectedPredefinedReason.ifBlank { notSaleReason.trim() }
                                }
                                onSave(notes.trim(), outcome, fullReason)
                            }
                        }
                    ) {
                        Text("Record Follow-up")
                    }
                }
            }
        }
    }
}
}

// ---------------------------------------------------------------------------------
// 6B. Assign Counsellor Dialog (Enforces 7-Counselling Rule)
// ---------------------------------------------------------------------------------
@Composable
fun AssignCounsellorDialog(
    guest: GuestPipelineEntity,
    distributors: List<UserEntity>,
    counsellingCountMap: Map<String, Int>,
    onDismiss: () -> Unit,
    onSelectDistributor: (UserEntity) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign L2 Counsellor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(
                    text = "Candidate: ${guest.guestName} (${guest.phoneNumber})",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rule 7: A distributor cannot take counselling for more than 7 persons. Any distributor at 7/7 is locked.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distributors.forEach { dist ->
                        val count = counsellingCountMap[dist.id] ?: 0
                        val isMaxReached = count >= 7

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMaxReached) Color(0xFFFEE2E2) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isMaxReached) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isMaxReached) {
                                    onSelectDistributor(dist)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = dist.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isMaxReached) Color(0xFF991B1B) else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "${dist.username} • ${dist.teamName}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isMaxReached) Color(0xFFDC2626) else if (count >= 5) Color(0xFFF59E0B) else Color(0xFF10B981)
                                ) {
                                    Text(
                                        text = if (isMaxReached) "7/7 (FULL)" else "$count/7 Taken",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        }
    }
}
}

// ---------------------------------------------------------------------------------
// 7. Sales Closing Entry (Requirement 10)
// ---------------------------------------------------------------------------------
@Composable
fun SalesClosingDialog(
    guest: GuestPipelineEntity,
    onDismiss: () -> Unit,
    onSave: (totalAmount: Double, paymentMode: String, txnRef: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("15000") }
    var paymentMode by remember { mutableStateOf("UPI_TRANSFER") }
    var txnRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
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
                    text = "Final Sales Closing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )
                Text(
                    text = "Candidate: ${guest.guestName} (Temp ID: ${guest.temporaryId})",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                if (guest.seniorityAmount > 0) {
                    Text(
                        text = "Seniority Amount already paid: ₹${guest.seniorityAmount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Full / Balance Package Amount (₹)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Closing Payment Mode:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMode == "HAND_CASH",
                        onClick = { paymentMode = "HAND_CASH" }
                    )
                    Text("Hand Cash", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMode == "BANK_TRANSFER",
                        onClick = { paymentMode = "BANK_TRANSFER" }
                    )
                    Text("Bank Transfer", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = paymentMode == "UPI_TRANSFER",
                        onClick = { paymentMode = "UPI_TRANSFER" }
                    )
                    Text("UPI Transfer", fontSize = 13.sp)
                }

                if (paymentMode == "BANK_TRANSFER" || paymentMode == "UPI_TRANSFER") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = txnRef,
                        onValueChange = {
                            txnRef = it
                            errorMsg = null
                        },
                        label = { Text("Transaction Reference Number*") },
                        placeholder = { Text("e.g. UPI/2026/89412 or Bank Ref") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Sales Closing Remarks / Notes") },
                    placeholder = { Text("e.g. Kit issued, ID card initiated, distributor joining completed") },
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
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                errorMsg = "Please enter a valid package amount."
                            } else if ((paymentMode == "BANK_TRANSFER" || paymentMode == "UPI_TRANSFER") && txnRef.isBlank()) {
                                errorMsg = "Transaction Reference Number is required."
                            } else {
                                onSave(amt, paymentMode, txnRef.trim(), notes.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Text("Complete Sale (Closing)")
                    }
                }
            }
        }
    }
}
}
