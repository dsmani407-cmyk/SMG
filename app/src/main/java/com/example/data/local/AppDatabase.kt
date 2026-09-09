package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AdPostEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CallLogEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GuestPipelineEntity
import com.example.data.model.TeamEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserEntity::class,
        TeamEntity::class,
        DailyTaskEntity::class,
        AdPostEntity::class,
        CallLogEntity::class,
        GuestPipelineEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun adPostDao(): AdPostDao
    abstract fun callLogDao(): CallLogDao
    abstract fun guestPipelineDao(): GuestPipelineDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_group_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                val teamDao = database.teamDao()
                val userDao = database.userDao()
                val auditLogDao = database.auditLogDao()

                // Initial Teams
                val phoenixTeam = TeamEntity(
                    id = "team_phoenix",
                    name = "Phoenix Team",
                    leaderId = "leader_1",
                    leaderName = "Rajesh Kannan (TL)"
                )
                val titanTeam = TeamEntity(
                    id = "team_titan",
                    name = "Titan Team",
                    leaderId = "leader_2",
                    leaderName = "Vigneshwaran (TL)"
                )
                teamDao.insertTeams(listOf(phoenixTeam, titanTeam))

                // Initial Users
                val admin = UserEntity(
                    id = "admin_1",
                    username = "admin",
                    fullName = "Central Admin",
                    email = "admin@smartgroup.in",
                    phone = "9876543210",
                    password = "admin",
                    role = "ADMIN",
                    teamId = "admin_team",
                    teamName = "Head Office"
                )
                val tl1 = UserEntity(
                    id = "leader_1",
                    username = "leader1",
                    fullName = "Rajesh Kannan",
                    email = "rajesh@smartgroup.in",
                    phone = "9840112233",
                    password = "leader",
                    role = "TEAM_LEADER",
                    teamId = "team_phoenix",
                    teamName = "Phoenix Team"
                )
                val tl2 = UserEntity(
                    id = "leader_2",
                    username = "leader2",
                    fullName = "Vigneshwaran",
                    email = "vignesh@smartgroup.in",
                    phone = "9840223344",
                    password = "leader",
                    role = "TEAM_LEADER",
                    teamId = "team_titan",
                    teamName = "Titan Team"
                )
                val dist1 = UserEntity(
                    id = "dist_1",
                    username = "mani",
                    fullName = "D.S. Mani",
                    email = "d.s.mani407@gmail.com",
                    phone = "9840556677",
                    password = "mani",
                    role = "DISTRIBUTOR",
                    teamId = "team_phoenix",
                    teamName = "Phoenix Team"
                )
                val dist2 = UserEntity(
                    id = "dist_2",
                    username = "kavitha",
                    fullName = "Kavitha Raj",
                    email = "kavitha@smartgroup.in",
                    phone = "9840778899",
                    password = "kavitha",
                    role = "DISTRIBUTOR",
                    teamId = "team_phoenix",
                    teamName = "Phoenix Team"
                )
                val dist3 = UserEntity(
                    id = "dist_3",
                    username = "suresh",
                    fullName = "Suresh Kumar",
                    email = "suresh@smartgroup.in",
                    phone = "9840990011",
                    password = "suresh",
                    role = "DISTRIBUTOR",
                    teamId = "team_titan",
                    teamName = "Titan Team"
                )
                userDao.insertUsers(listOf(admin, tl1, tl2, dist1, dist2, dist3))

                // Initial Seed Activity for demo
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                // Sample 5 ad posts for dist1
                val defaultSources = listOf(
                    "WhatsApp Status / Broadcast",
                    "Instagram Story & Reels",
                    "Facebook Career Groups",
                    "Telegram Jobs Channel",
                    "LinkedIn / Quikr Portal"
                )
                val sampleAdPosts = defaultSources.mapIndexed { index, source ->
                    AdPostEntity(
                        userId = dist1.id,
                        userName = dist1.fullName,
                        teamId = dist1.teamId,
                        date = today,
                        sourceIndex = index + 1,
                        sourceName = source,
                        postDetails = "Smart Group Business Opportunity Ad #${index + 1}",
                        postTime = "09:30 AM",
                        isPosted = index < 3 // 3 posted initially
                    )
                }
                database.adPostDao().insertAdPosts(sampleAdPosts)

                // Sample Task for dist1
                database.dailyTaskDao().insertOrUpdateTask(
                    DailyTaskEntity(
                        userId = dist1.id,
                        userName = dist1.fullName,
                        teamId = dist1.teamId,
                        date = today,
                        morningPlanning = true,
                        adPostingsDone = true,
                        twentyCallsDone = false,
                        guestFollowUpsDone = false,
                        eveningCheckoutStatus = "IN_PROGRESS",
                        eveningCheckoutTime = "",
                        eveningNotes = "Active day: Ad posts completed in 3 sources, starting 20 call campaign."
                    )
                )

                // Sample Call Logs for dist1 (Pre-filling 5 of 20 calls)
                val sampleCalls = listOf(
                    CallLogEntity(
                        userId = dist1.id,
                        userName = dist1.fullName,
                        teamId = dist1.teamId,
                        date = today,
                        callNumber = 1,
                        contactName = "R. Anbuchelvan",
                        phoneNumber = "+91 97890 12345",
                        callTime = "10:15 AM",
                        callResponse = "Confirmed Visit",
                        isConfirmed = true,
                        confirmationDate = today,
                        scheduledVisitDate = today,
                        scheduledVisitTime = "02:30 PM",
                        notes = "Very interested in distributor opportunity, visiting office today"
                    ),
                    CallLogEntity(
                        userId = dist1.id,
                        userName = dist1.fullName,
                        teamId = dist1.teamId,
                        date = today,
                        callNumber = 2,
                        contactName = "P. Saravanan",
                        phoneNumber = "+91 94441 56789",
                        callTime = "10:35 AM",
                        callResponse = "Callback",
                        notes = "In meeting, requested callback around 5 PM"
                    ),
                    CallLogEntity(
                        userId = dist1.id,
                        userName = dist1.fullName,
                        teamId = dist1.teamId,
                        date = today,
                        callNumber = 3,
                        contactName = "M. Meena Devi",
                        phoneNumber = "+91 98840 67890",
                        callTime = "11:00 AM",
                        callResponse = "Confirmed Visit",
                        isConfirmed = true,
                        confirmationDate = today,
                        scheduledVisitDate = today,
                        scheduledVisitTime = "04:00 PM",
                        notes = "Confirmed office visit for file registration"
                    )
                )
                database.callLogDao().insertCallLogs(sampleCalls)

                // Sample Guest Pipeline Entry (Anbuchelvan)
                database.guestPipelineDao().insertOrUpdateGuest(
                    GuestPipelineEntity(
                        guestName = "R. Anbuchelvan",
                        phoneNumber = "+91 97890 12345",
                        distributorId = dist1.id,
                        distributorName = dist1.fullName,
                        teamId = dist1.teamId,
                        scheduledVisitDate = today,
                        hasArrived = true,
                        arrivalTime = "02:30 PM",
                        fileProcessed = true,
                        fileProcessedBy = "D.S. Mani",
                        registrationFeePaid = true,
                        registrationPaymentMode = "UPI_TRANSFER",
                        registrationTxnRef = "UPI/2026/894723019",
                        registrationTimestamp = "02:45 PM",
                        counsellingStarted = false
                    )
                )

                auditLogDao.insertLog(
                    AuditLogEntity(
                        userId = "system",
                        userName = "System Boot",
                        action = "INITIALIZATION",
                        details = "Smart Group database initialized with Phoenix and Titan teams."
                    )
                )
            }
        }
    }
}
