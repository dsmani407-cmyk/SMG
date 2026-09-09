package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.firestore.FirestoreManager
import com.example.data.local.AppDatabase
import com.example.data.model.AdPostEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CallLogEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.SeniorityPaymentItem
import com.example.data.model.TeamEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.parseSeniorityPaymentsJson
import com.example.data.model.serializeSeniorityPaymentsJson
import com.example.data.repository.SmartGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmartGroupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmartGroupRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = SmartGroupRepository(database)
        // Initialize persistent cloud-synced storage via Firebase Firestore
        try {
            FirestoreManager.firestore
        } catch (e: Exception) {
            android.util.Log.w("SmartGroupViewModel", "Firestore lazy init notice: ${e.message}")
        }
    }

    // Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow("HOME") // HOME, TASKS, ADS, CALLS, PIPELINE, TEAM, ADMIN, CLOUD
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    // Alert Pop-up States
    // Requirement 7: A distributor cannot take counselling for more than 7 persons. No 8th counselling alert pop up!
    private val _counsellingLimitAlert = MutableStateFlow<String?>(null)
    val counsellingLimitAlert: StateFlow<String?> = _counsellingLimitAlert.asStateFlow()

    // Requirement 10: When sales closing complete, show Congratulations popup notification!
    private val _salesCelebration = MutableStateFlow<GuestPipelineEntity?>(null)
    val salesCelebration: StateFlow<GuestPipelineEntity?> = _salesCelebration.asStateFlow()

    // Cloud Sync Status Simulation
    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastCloudSyncTime = MutableStateFlow(getCurrentTimeString())
    val lastCloudSyncTime: StateFlow<String> = _lastCloudSyncTime.asStateFlow()

    // Base Data Flows
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeams: StateFlow<List<TeamEntity>> = repository.getAllTeams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Role-filtered Data Flows
    val currentTasks: StateFlow<List<DailyTaskEntity>> = combine(
        _currentUser,
        repository.getAllTasks()
    ) { user, tasks ->
        when (user?.role) {
            UserRole.ADMIN.name -> tasks
            UserRole.TEAM_LEADER.name -> tasks.filter { it.teamId == user.teamId }
            UserRole.DISTRIBUTOR.name -> tasks.filter { it.userId == user.id }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentAdPosts: StateFlow<List<AdPostEntity>> = combine(
        _currentUser,
        repository.getAllAdPosts()
    ) { user, posts ->
        when (user?.role) {
            UserRole.ADMIN.name -> posts
            UserRole.TEAM_LEADER.name -> posts.filter { it.teamId == user.teamId }
            UserRole.DISTRIBUTOR.name -> posts.filter { it.userId == user.id }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCallLogs: StateFlow<List<CallLogEntity>> = combine(
        _currentUser,
        _searchQuery,
        repository.getAllCallLogs()
    ) { user, query, calls ->
        val filteredByRole = when (user?.role) {
            UserRole.ADMIN.name -> calls
            UserRole.TEAM_LEADER.name -> calls.filter { it.teamId == user.teamId }
            UserRole.DISTRIBUTOR.name -> calls.filter { it.userId == user.id }
            else -> emptyList()
        }
        if (query.isBlank()) {
            filteredByRole
        } else {
            filteredByRole.filter {
                it.contactName.contains(query, ignoreCase = true) ||
                it.phoneNumber.contains(query, ignoreCase = true) ||
                it.callResponse.contains(query, ignoreCase = true) ||
                it.scheduledVisitDate.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGuests: StateFlow<List<GuestPipelineEntity>> = combine(
        _currentUser,
        _searchQuery,
        repository.getAllGuests()
    ) { user, query, guests ->
        val filteredByRole = when (user?.role) {
            UserRole.ADMIN.name -> guests
            UserRole.TEAM_LEADER.name -> guests.filter { it.teamId == user.teamId }
            UserRole.DISTRIBUTOR.name -> guests.filter { it.distributorId == user.id }
            else -> emptyList()
        }
        if (query.isBlank()) {
            filteredByRole
        } else {
            filteredByRole.filter {
                it.guestName.contains(query, ignoreCase = true) ||
                it.phoneNumber.contains(query, ignoreCase = true) ||
                it.temporaryId.contains(query, ignoreCase = true) ||
                it.distributorName.contains(query, ignoreCase = true) ||
                it.fileProcessedBy.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Authentication ---
    fun login(usernameOrEmail: String, pass: String) {
        viewModelScope.launch {
            _loginError.value = null

            // Background attempt to authenticate with Firebase Auth
            val fbResult = AuthManager.signInWithEmailPassword(usernameOrEmail, pass)

            val user = repository.authenticate(usernameOrEmail, pass)
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = "HOME"
                // Ensure initial 5 ads and 20 calls exist for today if distributor
                if (user.role == UserRole.DISTRIBUTOR.name) {
                    ensureDailyEntriesForDistributor(user)
                }
                val syncStatus = if (fbResult.isSuccess) "Firebase Connected" else "Cloud Sync Ready"
                triggerCloudSync("User ${user.fullName} logged in ($syncStatus)")
            } else {
                _loginError.value = "Invalid Login ID or Password. Please try again or use Forgot Password."
            }
        }
    }

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _loginError.value = null
            val result = AuthManager.signInWithGoogleCredentialManager(context)
            result.onSuccess { authUser ->
                val users = allUsers.value
                val existing = users.firstOrNull {
                    it.email.equals(authUser.email, ignoreCase = true) || it.id == authUser.uid
                }
                val loggedUser = existing ?: UserEntity(
                    id = authUser.uid.take(16),
                    username = authUser.email?.substringBefore("@") ?: "user_${authUser.uid.take(6)}",
                    password = "oauth_${authUser.uid.take(8)}",
                    fullName = authUser.displayName ?: "Distributor",
                    role = UserRole.DISTRIBUTOR.name,
                    phone = "",
                    email = authUser.email ?: "",
                    teamId = "phoenix",
                    teamName = "Phoenix Titans"
                ).also {
                    repository.createUser(it)
                }
                _currentUser.value = loggedUser
                _currentScreen.value = "HOME"
                ensureDailyEntriesForDistributor(loggedUser)
                triggerCloudSync("Google Credential Sign-in: ${loggedUser.fullName}")
            }.onFailure { err ->
                _loginError.value = "Google sign-in: ${err.localizedMessage ?: "Authentication canceled"}"
            }
        }
    }

    fun quickDemoLogin(role: UserRole) {
        viewModelScope.launch {
            val users = allUsers.value.ifEmpty {
                repository.getAllUsers().firstOrNull() ?: emptyList()
            }
            val user = users.firstOrNull { it.role == role.name }
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = "HOME"
                if (user.role == UserRole.DISTRIBUTOR.name) {
                    ensureDailyEntriesForDistributor(user)
                }
                triggerCloudSync("Switched to ${user.fullName} (${user.role})")
            }
        }
    }

    fun logout() {
        AuthManager.signOut()
        _currentUser.value = null
        _currentScreen.value = "LOGIN"
    }

    // --- Forgot Password Flow (Firebase sendPasswordResetEmail & In-App Reset) ---
    fun sendFirebasePasswordResetEmail(
        emailOrIdentifier: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmed = emailOrIdentifier.trim()
        if (trimmed.isBlank()) {
            onResult(false, "Please enter your registered email address or login ID.")
            return
        }

        viewModelScope.launch {
            // Resolve registered email if username is provided
            val localUser = repository.findUserForPasswordReset(trimmed)
            val targetEmail = if (localUser != null && localUser.email.isNotBlank() && localUser.email.contains("@")) {
                localUser.email
            } else if (trimmed.contains("@")) {
                trimmed
            } else {
                localUser?.email?.ifBlank { "$trimmed@smartgroup.app" } ?: "$trimmed@smartgroup.app"
            }

            val result = AuthManager.sendPasswordReset(targetEmail)
            result.onSuccess { sentEmail ->
                _infoMessage.value = "Password reset email sent to $sentEmail. Check your inbox."
                triggerCloudSync("Password reset email sent to $sentEmail")
                onResult(
                    true,
                    "Password reset email sent to $sentEmail. Please check your inbox or spam folder and follow the instructions to reset your password."
                )
            }.onFailure { err ->
                val fallbackMsg = err.localizedMessage ?: "Failed to send reset email. Please verify email or contact Admin."
                onResult(false, fallbackMsg)
            }
        }
    }

    fun requestPasswordReset(identifier: String, onUserFound: (UserEntity) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.findUserForPasswordReset(identifier)
            if (user != null) {
                onUserFound(user)
            } else {
                onError("No registered distributor or team member found with '$identifier'.")
            }
        }
    }

    fun completePasswordReset(userId: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.resetPassword(userId, newPass)
            _infoMessage.value = "Password changed successfully! You can now log in with your new password."
            triggerCloudSync("Password reset completed for user ID $userId")
            onSuccess()
        }
    }

    // --- Navigation & Search ---
    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun dismissCounsellingAlert() {
        _counsellingLimitAlert.value = null
    }

    fun dismissSalesCelebration() {
        _salesCelebration.value = null
    }

    // --- Daily Tasks (Requirement 3) ---
    fun updateDailyTask(
        morningPlanning: Boolean,
        adPostingsDone: Boolean,
        twentyCallsDone: Boolean,
        guestFollowUpsDone: Boolean,
        eveningStatus: String,
        eveningNotes: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = _selectedDate.value
            val existing = currentTasks.value.firstOrNull { it.userId == user.id && it.date == today }
            val updated = (existing ?: DailyTaskEntity(
                userId = user.id,
                userName = user.fullName,
                teamId = user.teamId,
                date = today
            )).copy(
                morningPlanning = morningPlanning,
                adPostingsDone = adPostingsDone,
                twentyCallsDone = twentyCallsDone,
                guestFollowUpsDone = guestFollowUpsDone,
                eveningCheckoutStatus = eveningStatus,
                eveningCheckoutTime = if (eveningStatus == "COMPLETED") getCurrentTimeString() else existing?.eveningCheckoutTime ?: "",
                eveningNotes = eveningNotes,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDailyTask(updated)
            _infoMessage.value = "Daily Task & Evening Checkout updated successfully!"
            triggerCloudSync("Daily task updated for ${user.fullName}")
        }
    }

    fun saveDailyTaskDirect(task: DailyTaskEntity) {
        viewModelScope.launch {
            repository.saveDailyTask(task.copy(updatedAt = System.currentTimeMillis()))
            _infoMessage.value = "Daily task for ${task.userName} saved successfully!"
            triggerCloudSync("Daily task updated for ${task.userName}")
        }
    }

    fun deleteDailyTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteDailyTask(taskId)
            _infoMessage.value = "Daily task record deleted."
            triggerCloudSync("Daily task #$taskId deleted")
        }
    }

    // --- 5 Ad Posts (Requirement 4) ---
    fun updateAdPost(post: AdPostEntity) {
        viewModelScope.launch {
            repository.saveAdPost(post.copy(updatedAt = System.currentTimeMillis()))
            triggerCloudSync("Ad post #${post.sourceIndex} updated: ${post.sourceName}")
        }
    }

    // --- 20 Calls Log & Scheduling (Requirement 5) ---
    fun saveCallLog(call: CallLogEntity) {
        viewModelScope.launch {
            repository.saveCallLog(call.copy(updatedAt = System.currentTimeMillis()))
            _infoMessage.value = "Call #${call.callNumber} updated. ${if (call.isConfirmed) "Guest scheduled for office visit on ${call.scheduledVisitDate}!" else ""}"
            triggerCloudSync("Call #${call.callNumber} saved by ${call.userName}")
        }
    }

    // --- Office Arrival, File Process & ₹150 Fee (Requirement 6) ---
    fun updateOfficeArrivalAndFileProcess(
        guestId: Long,
        hasArrived: Boolean,
        fileProcessed: Boolean,
        fileProcessedBy: String,
        feePaid: Boolean,
        paymentMode: String,
        txnRef: String
    ) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch
            val updated = guest.copy(
                hasArrived = hasArrived,
                arrivalTime = if (hasArrived && guest.arrivalTime.isBlank()) getCurrentTimeString() else guest.arrivalTime,
                fileProcessed = fileProcessed,
                fileProcessedBy = fileProcessedBy,
                registrationFeePaid = feePaid,
                registrationPaymentMode = if (feePaid) paymentMode else "NOT_PAID",
                registrationTxnRef = if (feePaid) txnRef else "",
                registrationTimestamp = if (feePaid) getCurrentTimeString() else "",
                updatedAt = System.currentTimeMillis()
            )
            repository.saveGuest(updated)
            _infoMessage.value = "Guest file & ₹150 registration fee details updated!"
            triggerCloudSync("Guest file processed: ${guest.guestName} by $fileProcessedBy")
        }
    }

    // --- Refund ₹150 Registration Fee ---
    fun refundRegistrationFee(guestId: Long, reason: String) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch
            val updated = guest.copy(
                registrationRefunded = true,
                registrationRefundReason = reason.trim(),
                registrationRefundTimestamp = "${getTodayDateString()} ${getCurrentTimeString()}",
                updatedAt = System.currentTimeMillis()
            )
            repository.saveGuest(updated)
            _infoMessage.value = "Registration fee ₹150 refunded for ${guest.guestName}."
            triggerCloudSync("Registration fee ₹150 refunded for ${guest.guestName}. Reason: $reason")
        }
    }

    // --- L2 Counselling (Requirement 7 - Max 7 limit rule) ---
    fun startCounselling(guestId: Long, counsellorId: String, counsellorName: String) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch

            // Check how many guests this distributor has already taken counselling for
            val currentCounsellingCount = repository.countCounsellingForDistributor(counsellorId)

            if (currentCounsellingCount >= 7) {
                // EXCEEDED MAXIMUM LIMIT! SHOW ALERT DIALOG (Requirement 7)
                _counsellingLimitAlert.value = "No 8th Counselling (L2) allowed!\n\nDistributor '$counsellorName' has already taken counselling for 7 candidates. As per Smart Group rules, a distributor cannot take counselling for more than 7 persons. Please assign another eligible distributor."
                return@launch
            }

            val updated = guest.copy(
                counsellingStarted = true,
                counsellorId = counsellorId,
                counsellorName = counsellorName,
                counsellingStartTime = getCurrentTimeString(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveGuest(updated)
            _infoMessage.value = "Counselling (L2) started for ${guest.guestName} by $counsellorName (${currentCounsellingCount + 1}/7)."
            triggerCloudSync("L2 Counselling started for ${guest.guestName}")
        }
    }

    fun completeCounselling(guestId: Long, notes: String) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch
            val updated = guest.copy(
                counsellingCompleted = true,
                counsellingNotes = notes,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveGuest(updated)
            _infoMessage.value = "Counselling (L2) completed. Proceed to Seniority Procedure."
            triggerCloudSync("L2 Counselling completed for ${guest.guestName}")
        }
    }

    // --- Seniority Procedure & Temp ID with Multi-Stage Payment & Voucher (Requirement 8) ---
    fun addSeniorityPayment(
        guestId: Long,
        amount: Double,
        paymentMode: String,
        txnRef: String,
        notes: String = "",
        voucherCollected: Boolean = false,
        voucherNumber: String = ""
    ) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch
            val existingPayments = parseSeniorityPaymentsJson(guest.seniorityPaymentsJson).toMutableList()
            val newPayment = SeniorityPaymentItem(
                id = java.util.UUID.randomUUID().toString(),
                amount = amount,
                paymentMode = paymentMode,
                txnRef = txnRef,
                timestamp = "${getTodayDateString()} ${getCurrentTimeString()}",
                notes = notes
            )
            existingPayments.add(newPayment)
            val updatedPaymentsJson = serializeSeniorityPaymentsJson(existingPayments)
            val cumulativeAmount = existingPayments.sumOf { it.amount }
            val tempId = guest.temporaryId.ifBlank { repository.generateTemporaryId() }

            val updated = guest.copy(
                seniorityCompleted = true,
                seniorityAmount = cumulativeAmount,
                seniorityPaymentMode = paymentMode,
                seniorityTxnRef = txnRef,
                seniorityDateTime = "${getTodayDateString()} ${getCurrentTimeString()}",
                seniorityPaymentsJson = updatedPaymentsJson,
                seniorityVoucherCollected = if (voucherCollected) true else guest.seniorityVoucherCollected,
                seniorityVoucherNumber = if (voucherNumber.isNotBlank()) voucherNumber else guest.seniorityVoucherNumber,
                temporaryId = tempId,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveGuest(updated)
            _infoMessage.value = "Seniority Payment ₹$amount added! Total: ₹$cumulativeAmount. Temp ID: $tempId"
            triggerCloudSync("Seniority payment added for ${guest.guestName}: ₹$amount (Total ₹$cumulativeAmount), Temp ID $tempId")
        }
    }

    fun completeSeniority(
        guestId: Long,
        amount: Double,
        paymentMode: String,
        txnRef: String
    ) {
        addSeniorityPayment(
            guestId = guestId,
            amount = amount,
            paymentMode = paymentMode,
            txnRef = txnRef,
            notes = "Initial Seniority"
        )
    }

    // --- Seniority Refund (if candidate cancels before closing) ---
    fun refundSeniority(guestId: Long, refundAmount: Double, reason: String) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch
            val updated = guest.copy(
                seniorityRefunded = true,
                seniorityRefundAmount = refundAmount,
                seniorityRefundReason = reason.trim(),
                seniorityRefundDateTime = "${getTodayDateString()} ${getCurrentTimeString()}",
                updatedAt = System.currentTimeMillis()
            )
            repository.saveGuest(updated)
            _infoMessage.value = "Seniority amount ₹$refundAmount refunded for ${guest.guestName}."
            triggerCloudSync("Seniority refunded for ${guest.guestName}: ₹$refundAmount. Reason: $reason")
        }
    }

    // --- L3 Sales Follow-up (Requirement 9 - 3 times max with Outcome & Reason) ---
    fun recordFollowUpWithOutcome(
        guestId: Long,
        followUpNumber: Int,
        notes: String,
        outcome: String = "PENDING", // PENDING, SALE, NOT_SALE
        notSaleReason: String = ""
    ) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch
            val today = getTodayDateString()
            var updated = when (followUpNumber) {
                1 -> guest.copy(
                    followUpCount = 1,
                    followUp1Date = today,
                    followUp1Notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
                2 -> guest.copy(
                    followUpCount = 2,
                    followUp2Date = today,
                    followUp2Notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
                3 -> guest.copy(
                    followUpCount = 3,
                    followUp3Date = today,
                    followUp3Notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
                else -> guest
            }
            if (outcome == "NOT_SALE") {
                updated = updated.copy(
                    finalOutcome = "NOT_SALE",
                    notSaleReason = notSaleReason.trim()
                )
            } else if (outcome == "SALE") {
                updated = updated.copy(
                    finalOutcome = "SALE"
                )
            }
            repository.saveGuest(updated)
            _infoMessage.value = "L3 Follow-up #$followUpNumber recorded (Outcome: $outcome)!"
            triggerCloudSync("L3 follow-up #$followUpNumber for ${guest.guestName}, Outcome: $outcome")
        }
    }

    fun addL3FollowUp(guestId: Long, followUpNumber: Int, notes: String) {
        recordFollowUpWithOutcome(guestId, followUpNumber, notes, outcome = "PENDING", notSaleReason = "")
    }

    // --- Sales Closing & Congratulations (Requirement 10) ---
    fun completeSalesClosing(
        guestId: Long,
        totalAmount: Double,
        paymentMode: String,
        txnRef: String,
        notes: String
    ) {
        viewModelScope.launch {
            val guest = currentGuests.value.firstOrNull { it.id == guestId } ?: return@launch
            val updated = guest.copy(
                salesClosed = true,
                totalPackageAmount = totalAmount,
                finalPaymentMode = paymentMode,
                finalTxnRef = txnRef,
                salesClosingDateTime = "${getTodayDateString()} ${getCurrentTimeString()}",
                salesClosingNotes = notes,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveGuest(updated)
            // Trigger Congratulations Pop-up!
            _salesCelebration.value = updated
            triggerCloudSync("SALES CLOSED: ${guest.guestName} - ₹$totalAmount by ${guest.distributorName}")
        }
    }

    // --- Team Leader & Admin Management (Requirement 11) ---
    fun addDistributor(
        fullName: String,
        username: String,
        phone: String,
        email: String,
        pass: String,
        targetTeamId: String,
        targetTeamName: String
    ) {
        viewModelScope.launch {
            val id = "dist_${System.currentTimeMillis()}"
            val newUser = UserEntity(
                id = id,
                username = username.trim().lowercase(),
                fullName = fullName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                password = pass,
                role = UserRole.DISTRIBUTOR.name,
                teamId = targetTeamId,
                teamName = targetTeamName
            )
            repository.createUser(newUser)

            // Register in Firebase Auth for cloud synchronization
            AuthManager.createUserWithEmailPassword(
                identifier = if (email.isNotBlank()) email else username,
                password = pass,
                displayName = fullName
            )

            _infoMessage.value = "Distributor '${fullName}' added to ${targetTeamName} successfully!"
            triggerCloudSync("New distributor added: $fullName to $targetTeamName")
        }
    }

    fun addTeam(name: String, leaderName: String) {
        viewModelScope.launch {
            val teamId = "team_${System.currentTimeMillis()}"
            val team = TeamEntity(
                id = teamId,
                name = name.trim(),
                leaderId = "leader_${System.currentTimeMillis()}",
                leaderName = leaderName.trim()
            )
            repository.createTeam(team)
            _infoMessage.value = "Team '$name' created successfully!"
            triggerCloudSync("Team created: $name")
        }
    }

    fun saveGuestDirect(guest: GuestPipelineEntity) {
        viewModelScope.launch {
            repository.saveGuest(guest)
            triggerCloudSync("Guest record updated: ${guest.guestName}")
        }
    }

    fun deleteGuest(guestId: Long) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteGuest(guestId, user.fullName)
            _infoMessage.value = "Candidate removed."
            triggerCloudSync("Guest record removed #$guestId by ${user.fullName}")
        }
    }

    fun deleteUser(userId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteUser(userId, user.fullName)
            _infoMessage.value = "User removed."
            triggerCloudSync("User removed #$userId by ${user.fullName}")
        }
    }

    fun deleteCallLog(callId: Long) {
        viewModelScope.launch {
            repository.deleteCallLog(callId)
            _infoMessage.value = "Call log removed."
            triggerCloudSync("Call log removed #$callId")
        }
    }

    fun adminDeleteGuest(guestId: Long) {
        val user = _currentUser.value ?: return
        if (user.role != UserRole.ADMIN.name) return
        viewModelScope.launch {
            repository.deleteGuest(guestId, user.fullName)
            _infoMessage.value = "Guest entry deleted by Admin."
            triggerCloudSync("Admin deleted guest #$guestId")
        }
    }

    fun adminDeleteUser(userId: String) {
        val user = _currentUser.value ?: return
        if (user.role != UserRole.ADMIN.name) return
        viewModelScope.launch {
            repository.deleteUser(userId, user.fullName)
            _infoMessage.value = "User deleted by Admin."
            triggerCloudSync("Admin deleted user #$userId")
        }
    }

    // Cloud Sync Trigger
    fun triggerCloudSync(actionDetails: String? = null) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val user = _currentUser.value
            if (user != null && actionDetails != null) {
                repository.logCloudAction(user, "CLOUD_SYNC", actionDetails)
            }

            // Push distributor and admin data to persistent cloud-synced storage (Firebase Firestore)
            try {
                FirestoreManager.syncAllLocalDataToFirestore(
                    users = allUsers.value,
                    teams = allTeams.value,
                    guests = currentGuests.value,
                    tasks = currentTasks.value,
                    calls = currentCallLogs.value,
                    adPosts = currentAdPosts.value
                )

                // Check for any remote users added on other phones and sync to local Room
                val remoteUsersResult = FirestoreManager.fetchRemoteUsers()
                if (remoteUsersResult.isSuccess) {
                    val remoteUsers = remoteUsersResult.getOrDefault(emptyList())
                    val localIds = allUsers.value.map { it.id }.toSet()
                    remoteUsers.filter { it.id !in localIds }.forEach { newRemoteUser ->
                        repository.createUser(newRemoteUser)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("SmartGroupViewModel", "Firestore sync exception handled: ${e.message}")
            }

            kotlinx.coroutines.delay(600) // Responsive cloud sync feedback
            _lastCloudSyncTime.value = getCurrentTimeString()
            _isCloudSyncing.value = false
        }
    }

    private suspend fun ensureDailyEntriesForDistributor(user: UserEntity) {
        val today = getTodayDateString()
        val existingPosts = repository.getAdPostsForUserAndDate(user.id, today).firstOrNull()
        if (existingPosts.isNullOrEmpty()) {
            repository.initializeDefaultAdPostsIfEmpty(user.id, user.fullName, user.teamId, today)
        }
        val existingCalls = repository.getCallLogsForUserAndDate(user.id, today).firstOrNull()
        if (existingCalls.isNullOrEmpty()) {
            repository.initialize20CallsIfEmpty(user.id, user.fullName, user.teamId, today)
        }
    }

    companion object {
        const val MAX_COUNSELLING_LIMIT = 7

        fun getTodayDateString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        fun getCurrentTimeString(): String {
            return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        }
    }
}
