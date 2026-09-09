package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.components.CongratulationsSalesDialog
import com.example.ui.components.CounsellingLimitAlertDialog
import com.example.ui.screens.AdPostingScreen
import com.example.ui.screens.AdminMasterScreen
import com.example.ui.screens.CallsTrackingScreen
import com.example.ui.screens.DailyTasksScreen
import com.example.ui.screens.DistributorHomeScreen
import com.example.ui.screens.GuestPipelineScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TeamManagementScreen

@Composable
fun SmartGroupApp(
    viewModel: SmartGroupViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val todayDate by viewModel.selectedDate.collectAsState()
    val isSyncing by viewModel.isCloudSyncing.collectAsState()
    val lastSyncTime by viewModel.lastCloudSyncTime.collectAsState()
    val counsellingAlert by viewModel.counsellingLimitAlert.collectAsState()
    val salesCelebration by viewModel.salesCelebration.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()

    // Data lists
    val allUsers by viewModel.allUsers.collectAsState()
    val allTeams by viewModel.allTeams.collectAsState()
    val currentTasks by viewModel.currentTasks.collectAsState()
    val currentAdPosts by viewModel.currentAdPosts.collectAsState()
    val currentCallLogs by viewModel.currentCallLogs.collectAsState()
    val currentGuests by viewModel.currentGuests.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(infoMessage) {
        infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearInfoMessage()
        }
    }

    if (currentUser == null) {
        val context = LocalContext.current
        LoginScreen(
            onLogin = { username, pass, _ ->
                viewModel.login(username, pass)
            },
            onQuickDemo = { roleStr ->
                val role = when (roleStr) {
                    "ADMIN" -> UserRole.ADMIN
                    "TEAM_LEADER" -> UserRole.TEAM_LEADER
                    else -> UserRole.DISTRIBUTOR
                }
                viewModel.quickDemoLogin(role)
            },
            onResetPassword = { identifier, newPass, onDone ->
                viewModel.requestPasswordReset(
                    identifier = identifier,
                    onUserFound = { user ->
                        viewModel.completePasswordReset(user.id, newPass) {
                            onDone(true, "Password for '${user.fullName}' updated successfully!")
                        }
                    },
                    onError = { err ->
                        onDone(false, err)
                    }
                )
            },
            onGoogleSignIn = {
                viewModel.loginWithGoogle(context)
            },
            onSendFirebaseResetEmail = { emailOrId, onResult ->
                viewModel.sendFirebasePasswordResetEmail(emailOrId, onResult)
            }
        )
    } else {
        val user = currentUser!!
        val activeTab = when (currentScreen) {
            "HOME" -> if (user.role == "ADMIN") "admin" else "dashboard"
            "TASKS" -> "tasks"
            "ADS" -> "ads"
            "CALLS" -> "calls"
            "PIPELINE" -> "pipeline"
            "TEAM" -> "team"
            "ADMIN", "CLOUD" -> "admin"
            else -> "dashboard"
        }

        Scaffold(
            topBar = {
                AppTopBar(
                    currentUser = user,
                    isCloudSyncing = isSyncing,
                    onSyncClick = { viewModel.triggerCloudSync("Manual Sync triggered by ${user.fullName}") },
                    onLogoutClick = { viewModel.logout() }
                )
            },
            bottomBar = {
                AppBottomNav(
                    userRole = user.role,
                    currentTab = activeTab,
                    onTabSelected = { tabId ->
                        when (tabId) {
                            "dashboard" -> viewModel.setScreen("HOME")
                            "tasks" -> viewModel.setScreen("TASKS")
                            "ads" -> viewModel.setScreen("ADS")
                            "calls" -> viewModel.setScreen("CALLS")
                            "pipeline" -> viewModel.setScreen("PIPELINE")
                            "team" -> viewModel.setScreen("TEAM")
                            "admin", "cloud" -> viewModel.setScreen("ADMIN")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        "HOME" -> {
                            if (user.role == "ADMIN") {
                                AdminMasterScreen(
                                    currentUser = user,
                                    allUsers = allUsers,
                                    allTeams = allTeams,
                                    allGuests = currentGuests,
                                    allTasks = currentTasks,
                                    allAdPosts = currentAdPosts,
                                    allCalls = currentCallLogs,
                                    auditLogs = auditLogs,
                                    isCloudSyncing = isSyncing,
                                    lastSyncTime = lastSyncTime,
                                    todayDate = todayDate,
                                    onBack = { viewModel.setScreen("HOME") },
                                    onTriggerCloudSync = { viewModel.triggerCloudSync("Manual cloud refresh by Admin") },
                                    onDeleteGuest = { gId -> viewModel.deleteGuest(gId) },
                                    onDeleteUser = { uId -> viewModel.deleteUser(uId) },
                                    onSaveTask = { task -> viewModel.saveDailyTaskDirect(task) },
                                    onDeleteTask = { taskId -> viewModel.deleteDailyTask(taskId) }
                                )
                            } else {
                                DistributorHomeScreen(
                                    currentUser = user,
                                    todayTask = currentTasks.firstOrNull { it.userId == user.id && it.date == todayDate },
                                    adPosts = currentAdPosts.filter { it.userId == user.id && it.date == todayDate },
                                    callLogs = currentCallLogs.filter { it.userId == user.id && it.date == todayDate },
                                    guests = currentGuests,
                                    onNavigate = { screenKey ->
                                        when (screenKey) {
                                            "TASKS", "tasks" -> viewModel.setScreen("TASKS")
                                            "ADS", "ads" -> viewModel.setScreen("ADS")
                                            "CALLS", "calls" -> viewModel.setScreen("CALLS")
                                            "PIPELINE", "pipeline" -> viewModel.setScreen("PIPELINE")
                                            "TEAM", "team" -> viewModel.setScreen("TEAM")
                                            "ADMIN", "admin" -> viewModel.setScreen("ADMIN")
                                            else -> viewModel.setScreen(screenKey)
                                        }
                                    }
                                )
                            }
                        }

                        "TASKS" -> {
                            DailyTasksScreen(
                                currentUser = user,
                                todayDate = todayDate,
                                tasks = currentTasks,
                                allUsers = allUsers,
                                onSaveTask = { updated ->
                                    viewModel.saveDailyTaskDirect(updated)
                                },
                                onDeleteTask = { taskId ->
                                    viewModel.deleteDailyTask(taskId)
                                }
                            )
                        }

                        "ADS" -> {
                            AdPostingScreen(
                                currentUser = user,
                                todayDate = todayDate,
                                adPosts = currentAdPosts,
                                onSavePost = { post ->
                                    viewModel.updateAdPost(post)
                                },
                                onDeletePost = { /* Admin delete */ }
                            )
                        }

                        "CALLS" -> {
                            CallsTrackingScreen(
                                currentUser = user,
                                todayDate = todayDate,
                                callLogs = currentCallLogs,
                                onSaveCall = { call ->
                                    viewModel.saveCallLog(call)
                                },
                                onDeleteCall = { callId ->
                                    viewModel.deleteCallLog(callId)
                                }
                            )
                        }

                        "PIPELINE" -> {
                            GuestPipelineScreen(
                                currentUser = user,
                                guests = currentGuests,
                                allDistributors = allUsers,
                                onSaveGuest = { g ->
                                    viewModel.saveGuestDirect(g)
                                },
                                onDeleteGuest = { gId ->
                                    viewModel.deleteGuest(gId)
                                },
                                onAttemptStartCounselling = { guest, counsellor ->
                                    viewModel.startCounselling(guest.id, counsellor.id, counsellor.fullName)
                                },
                                onCompleteSeniority = { guest, amt, mode, ref ->
                                    viewModel.completeSeniority(guest.id, amt, mode, ref)
                                },
                                onAddSeniorityPayment = { guest, amt, mode, ref, notes, voucherCollected, voucherNo ->
                                    viewModel.addSeniorityPayment(guest.id, amt, mode, ref, notes, voucherCollected, voucherNo)
                                },
                                onRefundRegistration = { guest, reason ->
                                    viewModel.refundRegistrationFee(guest.id, reason)
                                },
                                onRefundSeniority = { guest, amt, reason ->
                                    viewModel.refundSeniority(guest.id, amt, reason)
                                },
                                onAddFollowUp = { guest, notes ->
                                    val nextCount = (guest.followUpCount + 1).coerceAtMost(3)
                                    viewModel.addL3FollowUp(guest.id, nextCount, notes)
                                },
                                onAddFollowUpWithOutcome = { guest, notes, outcome, notSaleReason ->
                                    val nextCount = (guest.followUpCount + 1).coerceAtMost(3)
                                    viewModel.recordFollowUpWithOutcome(guest.id, nextCount, notes, outcome, notSaleReason)
                                },
                                onCompleteSalesClosing = { guest, amt, mode, ref, notes ->
                                    viewModel.completeSalesClosing(guest.id, amt, mode, ref, notes)
                                }
                            )
                        }

                        "TEAM" -> {
                            TeamManagementScreen(
                                currentUser = user,
                                users = allUsers,
                                onAddUser = { newUser ->
                                    viewModel.addDistributor(
                                        fullName = newUser.fullName,
                                        username = newUser.username,
                                        phone = newUser.phone,
                                        email = newUser.email,
                                        pass = newUser.password,
                                        targetTeamId = newUser.teamId,
                                        targetTeamName = newUser.teamName
                                    )
                                },
                                onDeleteUser = { uId ->
                                    viewModel.deleteUser(uId)
                                }
                            )
                        }

                        "ADMIN", "CLOUD" -> {
                            AdminMasterScreen(
                                currentUser = user,
                                allUsers = allUsers,
                                allTeams = allTeams,
                                allGuests = currentGuests,
                                allTasks = currentTasks,
                                allAdPosts = currentAdPosts,
                                allCalls = currentCallLogs,
                                auditLogs = auditLogs,
                                isCloudSyncing = isSyncing,
                                lastSyncTime = lastSyncTime,
                                todayDate = todayDate,
                                onBack = { viewModel.setScreen("HOME") },
                                onTriggerCloudSync = { viewModel.triggerCloudSync("Manual cloud refresh") },
                                onDeleteGuest = { gId -> viewModel.deleteGuest(gId) },
                                onDeleteUser = { uId -> viewModel.deleteUser(uId) },
                                onSaveTask = { task -> viewModel.saveDailyTaskDirect(task) },
                                onDeleteTask = { taskId -> viewModel.deleteDailyTask(taskId) }
                            )
                        }

                        else -> {
                            DistributorHomeScreen(
                                currentUser = user,
                                todayTask = currentTasks.firstOrNull { it.userId == user.id && it.date == todayDate },
                                adPosts = currentAdPosts.filter { it.userId == user.id && it.date == todayDate },
                                callLogs = currentCallLogs.filter { it.userId == user.id && it.date == todayDate },
                                guests = currentGuests,
                                onNavigate = { screenKey ->
                                    when (screenKey) {
                                        "TASKS", "tasks" -> viewModel.setScreen("TASKS")
                                        "ADS", "ads" -> viewModel.setScreen("ADS")
                                        "CALLS", "calls" -> viewModel.setScreen("CALLS")
                                        "PIPELINE", "pipeline" -> viewModel.setScreen("PIPELINE")
                                        "TEAM", "team" -> viewModel.setScreen("TEAM")
                                        "ADMIN", "admin" -> viewModel.setScreen("ADMIN")
                                        else -> viewModel.setScreen(screenKey)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Global Alert Dialog: Counselling Limit of 7 Exceeded! (Requirement 7)
        counsellingAlert?.let { alertMessage ->
            CounsellingLimitAlertDialog(
                message = alertMessage,
                onDismiss = { viewModel.dismissCounsellingAlert() }
            )
        }

        // Global Dialog: Sales Closing Congratulations! (Requirement 10)
        salesCelebration?.let { closedGuest ->
            CongratulationsSalesDialog(
                guest = closedGuest,
                distributorName = closedGuest.distributorName,
                onDismiss = { viewModel.dismissSalesCelebration() }
            )
        }
    }
}
