package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AdPostEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CallLogEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.TeamEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class SmartGroupRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val teamDao = database.teamDao()
    private val dailyTaskDao = database.dailyTaskDao()
    private val adPostDao = database.adPostDao()
    private val callLogDao = database.callLogDao()
    private val guestPipelineDao = database.guestPipelineDao()
    private val auditLogDao = database.auditLogDao()

    // --- Users & Teams ---
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getUsersByTeam(teamId: String): Flow<List<UserEntity>> = userDao.getUsersByTeam(teamId)
    fun getAllTeams(): Flow<List<TeamEntity>> = teamDao.getAllTeams()

    suspend fun authenticate(usernameOrEmail: String, password: String): UserEntity? {
        val user = userDao.getUserByUsername(usernameOrEmail.trim())
            ?: userDao.findUserByPhoneOrEmail(usernameOrEmail.trim())
        return if (user != null && user.password == password) user else null
    }

    suspend fun findUserForPasswordReset(identifier: String): UserEntity? {
        return userDao.findUserByPhoneOrEmail(identifier.trim())
            ?: userDao.getUserByUsername(identifier.trim())
    }

    suspend fun resetPassword(userId: String, newPass: String) {
        val user = userDao.getUserById(userId)
        if (user != null) {
            userDao.updateUser(user.copy(password = newPass))
            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = userId,
                    userName = user.fullName,
                    action = "PASSWORD_RESET",
                    details = "Password successfully updated for user ${user.username}"
                )
            )
        }
    }

    suspend fun createUser(user: UserEntity) {
        userDao.insertUser(user)
        auditLogDao.insertLog(
            AuditLogEntity(
                userId = user.id,
                userName = user.fullName,
                action = "USER_CREATED",
                details = "User ${user.fullName} (${user.role}) added to ${user.teamName}"
            )
        )
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
        auditLogDao.insertLog(
            AuditLogEntity(
                userId = user.id,
                userName = user.fullName,
                action = "USER_UPDATED",
                details = "User ${user.fullName} details updated"
            )
        )
    }

    suspend fun deleteUser(userId: String, adminName: String) {
        val user = userDao.getUserById(userId)
        userDao.deleteUserById(userId)
        auditLogDao.insertLog(
            AuditLogEntity(
                userId = "admin",
                userName = adminName,
                action = "USER_DELETED",
                details = "User ${user?.fullName ?: userId} deleted by admin"
            )
        )
    }

    suspend fun createTeam(team: TeamEntity) {
        teamDao.insertTeam(team)
    }

    // --- Daily Tasks ---
    fun getTodayTasksForUser(userId: String, date: String): Flow<DailyTaskEntity?> =
        dailyTaskDao.getTaskForUserAndDate(userId, date)

    fun getTasksForTeam(teamId: String): Flow<List<DailyTaskEntity>> =
        dailyTaskDao.getTasksForTeam(teamId)

    fun getAllTasks(): Flow<List<DailyTaskEntity>> = dailyTaskDao.getAllTasks()

    suspend fun saveDailyTask(task: DailyTaskEntity) {
        dailyTaskDao.insertOrUpdateTask(task)
        auditLogDao.insertLog(
            AuditLogEntity(
                userId = task.userId,
                userName = task.userName,
                action = "DAILY_TASK_UPDATED",
                details = "Task for ${task.date} updated. Evening checkout: ${task.eveningCheckoutStatus}"
            )
        )
    }

    suspend fun deleteDailyTask(taskId: Long) {
        dailyTaskDao.deleteTaskById(taskId)
    }

    // --- 5 Ad Posts ---
    fun getAdPostsForUserAndDate(userId: String, date: String): Flow<List<AdPostEntity>> =
        adPostDao.getAdPostsForUserAndDate(userId, date)

    fun getAdPostsForTeam(teamId: String): Flow<List<AdPostEntity>> =
        adPostDao.getAdPostsForTeam(teamId)

    fun getAllAdPosts(): Flow<List<AdPostEntity>> = adPostDao.getAllAdPosts()

    suspend fun saveAdPost(post: AdPostEntity) {
        adPostDao.insertOrUpdateAdPost(post)
    }

    suspend fun initializeDefaultAdPostsIfEmpty(userId: String, userName: String, teamId: String, date: String) {
        val defaultSources = listOf(
            "WhatsApp Status / Broadcast",
            "Instagram Story / Reel",
            "Facebook Career Groups",
            "Telegram Job Channels",
            "LinkedIn / Job Portal"
        )
        val posts = defaultSources.mapIndexed { index, source ->
            AdPostEntity(
                userId = userId,
                userName = userName,
                teamId = teamId,
                date = date,
                sourceIndex = index + 1,
                sourceName = source,
                postDetails = "",
                postTime = "",
                isPosted = false
            )
        }
        adPostDao.insertAdPosts(posts)
    }

    // --- 20 Calls Log ---
    fun getCallLogsForUserAndDate(userId: String, date: String): Flow<List<CallLogEntity>> =
        callLogDao.getCallLogsForUserAndDate(userId, date)

    fun getAllCallLogsForUser(userId: String): Flow<List<CallLogEntity>> =
        callLogDao.getAllCallLogsForUser(userId)

    fun getCallLogsForTeam(teamId: String): Flow<List<CallLogEntity>> =
        callLogDao.getCallLogsForTeam(teamId)

    fun getAllCallLogs(): Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()

    suspend fun saveCallLog(call: CallLogEntity): Long {
        val id = callLogDao.insertOrUpdateCallLog(call)
        // If confirmed visit, also sync/create pipeline entry automatically
        if (call.isConfirmed && call.scheduledVisitDate.isNotBlank()) {
            val existing = guestPipelineDao.getAllGuests()
            // create new pipeline guest
            val guest = GuestPipelineEntity(
                guestName = call.contactName.ifBlank { "Guest #${call.callNumber}" },
                phoneNumber = call.phoneNumber,
                distributorId = call.userId,
                distributorName = call.userName,
                teamId = call.teamId,
                scheduledVisitDate = call.scheduledVisitDate,
                callLogId = id
            )
            guestPipelineDao.insertOrUpdateGuest(guest)
        }
        return id
    }

    suspend fun deleteCallLog(id: Long) {
        callLogDao.deleteCallLogById(id)
    }

    suspend fun initialize20CallsIfEmpty(userId: String, userName: String, teamId: String, date: String) {
        val calls = (1..20).map { num ->
            CallLogEntity(
                userId = userId,
                userName = userName,
                teamId = teamId,
                date = date,
                callNumber = num,
                contactName = "",
                phoneNumber = "",
                callTime = "",
                callResponse = "Ringing",
                isConfirmed = false
            )
        }
        callLogDao.insertCallLogs(calls)
    }

    // --- Guest Pipeline & Counselling (Max 7 Rule) ---
    fun getGuestsForDistributor(distributorId: String): Flow<List<GuestPipelineEntity>> =
        guestPipelineDao.getGuestsForDistributor(distributorId)

    fun getGuestsForTeam(teamId: String): Flow<List<GuestPipelineEntity>> =
        guestPipelineDao.getGuestsForTeam(teamId)

    fun getAllGuests(): Flow<List<GuestPipelineEntity>> = guestPipelineDao.getAllGuests()

    suspend fun countCounsellingForDistributor(distributorId: String): Int {
        return guestPipelineDao.countCounsellingForDistributor(distributorId)
    }

    suspend fun saveGuest(guest: GuestPipelineEntity): Long {
        return guestPipelineDao.insertOrUpdateGuest(guest)
    }

    suspend fun deleteGuest(id: Long, user: String) {
        guestPipelineDao.deleteGuestById(id)
        auditLogDao.insertLog(
            AuditLogEntity(
                userId = "admin",
                userName = user,
                action = "GUEST_DELETED",
                details = "Guest record ID #$id deleted"
            )
        )
    }

    fun generateTemporaryId(): String {
        val rand = 1000 + Random().nextInt(9000)
        return "SG-TMP-2026-$rand"
    }

    // --- Audit Logs ---
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    suspend fun logCloudAction(user: UserEntity, action: String, details: String) {
        auditLogDao.insertLog(
            AuditLogEntity(
                userId = user.id,
                userName = user.fullName,
                action = action,
                details = details
            )
        )
    }
}
