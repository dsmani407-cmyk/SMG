package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ForgotPasswordDialog

@Composable
fun LoginScreen(
    onLogin: (username: String, pass: String, onError: (String) -> Unit) -> Unit,
    onQuickDemo: (role: String) -> Unit,
    onResetPassword: (identifier: String, newPass: String, onDone: (Boolean, String) -> Unit) -> Unit,
    onGoogleSignIn: (() -> Unit)? = null,
    onSendFirebaseResetEmail: ((emailOrIdentifier: String, onResult: (Boolean, String) -> Unit) -> Unit)? = null
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var infoToast by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_smart_groups_logo),
                    contentDescription = "Smart Groups Logo",
                    modifier = Modifier.size(82.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Smart Group",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "Enterprise Team & Distributor Management Portal",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Login Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Sign In to Your Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Enter your distributor or admin login ID and password",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("Login ID / Username", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. mani or leader1 or admin", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("Enter your password", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Forgot Password link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showForgotDialog = true },
                            modifier = Modifier.testTag("forgot_password_btn")
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    if (infoToast != null) {
                        Text(
                            text = infoToast ?: "",
                            color = Color(0xFF34D399),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both Login ID and Password."
                            } else {
                                onLogin(username, password) { err ->
                                    errorMessage = err
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_btn")
                    ) {
                        Text(
                            text = "Login to Smart Group",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (onGoogleSignIn != null) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "  OR  ",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        androidx.compose.material3.OutlinedButton(
                            onClick = onGoogleSignIn,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("google_signin_btn")
                        ) {
                            Text(
                                text = "🌐 Sign in with Google (Credential Manager)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛡️ Firebase Auth & Credential Manager Supported",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Switcher Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚀 One-Tap Demo Access",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCD34D),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Instant login to test role-specific dashboards & privacy controls:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Admin
                        QuickRoleChip(
                            label = "Admin",
                            sub = "Super Control",
                            icon = Icons.Default.AdminPanelSettings,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f)
                        ) {
                            onQuickDemo("ADMIN")
                        }

                        // Team Leader
                        QuickRoleChip(
                            label = "Leader 1",
                            sub = "Phoenix TL",
                            icon = Icons.Default.SupervisorAccount,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        ) {
                            onQuickDemo("TEAM_LEADER")
                        }

                        // Distributor
                        QuickRoleChip(
                            label = "Mani (Dist)",
                            sub = "Distributor",
                            icon = Icons.Default.Person,
                            color = Color(0xFF059669),
                            modifier = Modifier.weight(1f)
                        ) {
                            onQuickDemo("DISTRIBUTOR")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showForgotDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotDialog = false },
            onSubmit = { identifier, newPass ->
                onResetPassword(identifier, newPass) { success, message ->
                    showForgotDialog = false
                    if (success) {
                        infoToast = message
                        errorMessage = null
                    } else {
                        errorMessage = message
                    }
                }
            },
            onSendFirebaseResetEmail = onSendFirebaseResetEmail
        )
    }
}

@Composable
fun QuickRoleChip(
    label: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = sub,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
