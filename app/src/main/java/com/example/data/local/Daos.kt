package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdPostEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CallLogEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.TeamEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE active = 1")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE teamId = :teamId AND active = 1")
    fun getUsersByTeam(teamId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE (phone = :identifier OR email = :identifier) AND active = 1 LIMIT 1")
    suspend fun findUserByPhoneOrEmail(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :teamId LIMIT 1")
    suspend fun getTeamById(teamId: String): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Delete
    suspend fun deleteTeam(team: TeamEntity)
}

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE userId = :userId AND date = :date LIMIT 1")
    fun getTaskForUserAndDate(userId: String, date: String): Flow<DailyTaskEntity?>

    @Query("SELECT * FROM daily_tasks WHERE userId = :userId ORDER BY date DESC")
    fun getTasksForUser(userId: String): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE teamId = :teamId ORDER BY date DESC")
    fun getTasksForTeam(teamId: String): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks ORDER BY date DESC, updatedAt DESC")
    fun getAllTasks(): Flow<List<DailyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: DailyTaskEntity): Long

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface AdPostDao {
    @Query("SELECT * FROM ad_posts WHERE userId = :userId AND date = :date ORDER BY sourceIndex ASC")
    fun getAdPostsForUserAndDate(userId: String, date: String): Flow<List<AdPostEntity>>

    @Query("SELECT * FROM ad_posts WHERE teamId = :teamId ORDER BY date DESC, sourceIndex ASC")
    fun getAdPostsForTeam(teamId: String): Flow<List<AdPostEntity>>

    @Query("SELECT * FROM ad_posts ORDER BY date DESC, updatedAt DESC")
    fun getAllAdPosts(): Flow<List<AdPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAdPost(post: AdPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdPosts(posts: List<AdPostEntity>)

    @Query("DELETE FROM ad_posts WHERE id = :id")
    suspend fun deleteAdPostById(id: Long)
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs WHERE userId = :userId AND date = :date ORDER BY callNumber ASC")
    fun getCallLogsForUserAndDate(userId: String, date: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE userId = :userId ORDER BY date DESC, callNumber ASC")
    fun getAllCallLogsForUser(userId: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE teamId = :teamId ORDER BY date DESC, callNumber ASC")
    fun getCallLogsForTeam(teamId: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs ORDER BY date DESC, updatedAt DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallLog(log: CallLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLogs(logs: List<CallLogEntity>)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteCallLogById(id: Long)
}

@Dao
interface GuestPipelineDao {
    @Query("SELECT * FROM guest_pipeline WHERE distributorId = :distributorId ORDER BY updatedAt DESC")
    fun getGuestsForDistributor(distributorId: String): Flow<List<GuestPipelineEntity>>

    @Query("SELECT * FROM guest_pipeline WHERE teamId = :teamId ORDER BY updatedAt DESC")
    fun getGuestsForTeam(teamId: String): Flow<List<GuestPipelineEntity>>

    @Query("SELECT * FROM guest_pipeline ORDER BY updatedAt DESC")
    fun getAllGuests(): Flow<List<GuestPipelineEntity>>

    @Query("SELECT * FROM guest_pipeline WHERE id = :id LIMIT 1")
    suspend fun getGuestById(id: Long): GuestPipelineEntity?

    @Query("SELECT COUNT(*) FROM guest_pipeline WHERE counsellorId = :counsellorId AND counsellingStarted = 1")
    suspend fun countCounsellingForDistributor(counsellorId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGuest(guest: GuestPipelineEntity): Long

    @Query("DELETE FROM guest_pipeline WHERE id = :id")
    suspend fun deleteGuestById(id: Long)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}
