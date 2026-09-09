package com.example.data.firestore

import android.util.Log
import com.example.data.model.AdPostEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CallLogEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.TeamEntity
import com.example.data.model.UserEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Firestore persistent cloud-synced storage
 * for distributor, team leader, and admin records.
 */
object FirestoreManager {
    private const val TAG = "FirestoreManager"

    // Collection names
    const val COLL_USERS = "users"
    const val COLL_TEAMS = "teams"
    const val COLL_DAILY_TASKS = "daily_tasks"
    const val COLL_AD_POSTS = "ad_posts"
    const val COLL_CALL_LOGS = "call_logs"
    const val COLL_GUESTS = "guests"
    const val COLL_AUDIT_LOGS = "audit_logs"

    private var _firestoreInstance: FirebaseFirestore? = null

    val isAvailable: Boolean
        get() {
            return try {
                FirebaseApp.getApps(FirebaseFirestore.getInstance().app.applicationContext).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

    /**
     * Initializes and returns the Firebase Firestore instance with persistent offline caching.
     */
    val firestore: FirebaseFirestore?
        get() {
            if (_firestoreInstance != null) return _firestoreInstance

            return try {
                val db = FirebaseFirestore.getInstance()
                // Configure persistent local cache for high offline resilience
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder().build()
                    )
                    .build()
                db.firestoreSettings = settings
                _firestoreInstance = db
                Log.i(TAG, "Firebase Firestore initialized successfully with persistent cache")
                db
            } catch (e: Exception) {
                Log.w(TAG, "Firestore initialization notice: ${e.message}")
                null
            }
        }

    // --- User Synchronization (Distributor & Admin) ---

    suspend fun syncUser(user: UserEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        return try {
            val userMap = hashMapOf(
                "id" to user.id,
                "username" to user.username,
                "fullName" to user.fullName,
                "email" to user.email,
                "phone" to user.phone,
                "password" to user.password,
                "role" to user.role,
                "teamId" to user.teamId,
                "teamName" to user.teamName,
                "active" to user.active,
                "createdAt" to user.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLL_USERS).document(user.id)
                .set(userMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user ${user.id} to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        return try {
            db.collection(COLL_USERS).document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user $userId from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteUsers(): Result<List<UserEntity>> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        return try {
            val snapshot = db.collection(COLL_USERS).get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val username = doc.getString("username") ?: return@mapNotNull null
                val fullName = doc.getString("fullName") ?: ""
                val email = doc.getString("email") ?: ""
                val phone = doc.getString("phone") ?: ""
                val password = doc.getString("password") ?: "pass123"
                val role = doc.getString("role") ?: "DISTRIBUTOR"
                val teamId = doc.getString("teamId") ?: ""
                val teamName = doc.getString("teamName") ?: ""
                val active = doc.getBoolean("active") ?: true
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                UserEntity(
                    id = id,
                    username = username,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    password = password,
                    role = role,
                    teamId = teamId,
                    teamName = teamName,
                    active = active,
                    createdAt = createdAt
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching remote users from Firestore", e)
            Result.failure(e)
        }
    }

    // --- Team Synchronization ---

    suspend fun syncTeam(team: TeamEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        return try {
            val map = hashMapOf(
                "id" to team.id,
                "name" to team.name,
                "leaderId" to team.leaderId,
                "leaderName" to team.leaderName,
                "createdAt" to team.createdAt
            )
            db.collection(COLL_TEAMS).document(team.id)
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing team ${team.id}", e)
            Result.failure(e)
        }
    }

    // --- Daily Tasks Synchronization ---

    suspend fun syncDailyTask(task: DailyTaskEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        val docId = if (task.id > 0) "task_${task.id}" else "task_${task.userId}_${task.date}"
        return try {
            val map = hashMapOf(
                "id" to task.id,
                "userId" to task.userId,
                "userName" to task.userName,
                "teamId" to task.teamId,
                "date" to task.date,
                "morningPlanning" to task.morningPlanning,
                "adPostingsDone" to task.adPostingsDone,
                "twentyCallsDone" to task.twentyCallsDone,
                "guestFollowUpsDone" to task.guestFollowUpsDone,
                "eveningCheckoutStatus" to task.eveningCheckoutStatus,
                "eveningCheckoutTime" to task.eveningCheckoutTime,
                "eveningNotes" to task.eveningNotes,
                "customTasksJson" to task.customTasksJson,
                "updatedAt" to task.updatedAt
            )
            db.collection(COLL_DAILY_TASKS).document(docId)
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing daily task to Firestore", e)
            Result.failure(e)
        }
    }

    // --- Ad Posts Synchronization ---

    suspend fun syncAdPost(post: AdPostEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        val docId = if (post.id > 0) "ad_${post.id}" else "ad_${post.userId}_${post.date}_${post.sourceIndex}"
        return try {
            val map = hashMapOf(
                "id" to post.id,
                "userId" to post.userId,
                "userName" to post.userName,
                "teamId" to post.teamId,
                "date" to post.date,
                "sourceIndex" to post.sourceIndex,
                "sourceName" to post.sourceName,
                "postDetails" to post.postDetails,
                "postTime" to post.postTime,
                "isPosted" to post.isPosted,
                "updatedAt" to post.updatedAt
            )
            db.collection(COLL_AD_POSTS).document(docId)
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing ad post to Firestore", e)
            Result.failure(e)
        }
    }

    // --- Call Logs Synchronization ---

    suspend fun syncCallLog(call: CallLogEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        val docId = if (call.id > 0) "call_${call.id}" else "call_${call.userId}_${call.date}_${call.callNumber}"
        return try {
            val map = hashMapOf(
                "id" to call.id,
                "userId" to call.userId,
                "userName" to call.userName,
                "teamId" to call.teamId,
                "date" to call.date,
                "callNumber" to call.callNumber,
                "contactName" to call.contactName,
                "phoneNumber" to call.phoneNumber,
                "callTime" to call.callTime,
                "callResponse" to call.callResponse,
                "isConfirmed" to call.isConfirmed,
                "confirmationDate" to call.confirmationDate,
                "scheduledVisitDate" to call.scheduledVisitDate,
                "scheduledVisitTime" to call.scheduledVisitTime,
                "notes" to call.notes,
                "updatedAt" to call.updatedAt
            )
            db.collection(COLL_CALL_LOGS).document(docId)
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing call log to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCallLog(callId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        return try {
            db.collection(COLL_CALL_LOGS).document("call_$callId").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting call log $callId from Firestore", e)
            Result.failure(e)
        }
    }

    // --- Guest Pipeline Synchronization ---

    suspend fun syncGuest(guest: GuestPipelineEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        val docId = "guest_${guest.id}"
        return try {
            val map = hashMapOf(
                "id" to guest.id,
                "guestName" to guest.guestName,
                "phoneNumber" to guest.phoneNumber,
                "distributorId" to guest.distributorId,
                "distributorName" to guest.distributorName,
                "teamId" to guest.teamId,
                "scheduledVisitDate" to guest.scheduledVisitDate,
                "callLogId" to guest.callLogId,
                "hasArrived" to guest.hasArrived,
                "arrivalTime" to guest.arrivalTime,
                "fileProcessed" to guest.fileProcessed,
                "fileProcessedBy" to guest.fileProcessedBy,
                "registrationFeePaid" to guest.registrationFeePaid,
                "registrationPaymentMode" to guest.registrationPaymentMode,
                "registrationTxnRef" to guest.registrationTxnRef,
                "registrationTimestamp" to guest.registrationTimestamp,
                "counsellingStarted" to guest.counsellingStarted,
                "counsellorId" to guest.counsellorId,
                "counsellorName" to guest.counsellorName,
                "counsellingStartTime" to guest.counsellingStartTime,
                "counsellingCompleted" to guest.counsellingCompleted,
                "counsellingNotes" to guest.counsellingNotes,
                "seniorityCompleted" to guest.seniorityCompleted,
                "seniorityAmount" to guest.seniorityAmount,
                "seniorityPaymentMode" to guest.seniorityPaymentMode,
                "seniorityTxnRef" to guest.seniorityTxnRef,
                "seniorityDateTime" to guest.seniorityDateTime,
                "temporaryId" to guest.temporaryId,
                "followUpCount" to guest.followUpCount,
                "followUp1Date" to guest.followUp1Date,
                "followUp1Notes" to guest.followUp1Notes,
                "followUp2Date" to guest.followUp2Date,
                "followUp2Notes" to guest.followUp2Notes,
                "followUp3Date" to guest.followUp3Date,
                "followUp3Notes" to guest.followUp3Notes,
                "salesClosed" to guest.salesClosed,
                "totalPackageAmount" to guest.totalPackageAmount,
                "finalPaymentMode" to guest.finalPaymentMode,
                "finalTxnRef" to guest.finalTxnRef,
                "salesClosingDateTime" to guest.salesClosingDateTime,
                "salesClosingNotes" to guest.salesClosingNotes,
                "createdAt" to guest.createdAt,
                "updatedAt" to guest.updatedAt
            )
            db.collection(COLL_GUESTS).document(docId)
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing guest ${guest.id} to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteGuest(guestId: Long): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        return try {
            db.collection(COLL_GUESTS).document("guest_$guestId").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting guest $guestId from Firestore", e)
            Result.failure(e)
        }
    }

    // --- Audit Log Synchronization ---

    suspend fun syncAuditLog(log: AuditLogEntity): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore instance unavailable"))
        val docId = if (log.id > 0) "audit_${log.id}" else "audit_${System.currentTimeMillis()}"
        return try {
            val map = hashMapOf(
                "id" to log.id,
                "userId" to log.userId,
                "userName" to log.userName,
                "action" to log.action,
                "details" to log.details,
                "timestamp" to log.timestamp
            )
            db.collection(COLL_AUDIT_LOGS).document(docId)
                .set(map, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing audit log to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Performs a batch or parallel sync of local distributor and admin data into Firestore.
     */
    suspend fun syncAllLocalDataToFirestore(
        users: List<UserEntity>,
        teams: List<TeamEntity>,
        guests: List<GuestPipelineEntity>,
        tasks: List<DailyTaskEntity>,
        calls: List<CallLogEntity>,
        adPosts: List<AdPostEntity>
    ): Result<String> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            users.forEach { syncUser(it) }
            teams.forEach { syncTeam(it) }
            guests.forEach { syncGuest(it) }
            tasks.forEach { syncDailyTask(it) }
            calls.forEach { syncCallLog(it) }
            adPosts.forEach { syncAdPost(it) }

            Result.success("Synced ${users.size} users, ${teams.size} teams, and ${guests.size} pipeline records to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed during syncAllLocalDataToFirestore", e)
            Result.failure(e)
        }
    }
}
