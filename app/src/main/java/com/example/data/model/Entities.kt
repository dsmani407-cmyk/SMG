package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    TEAM_LEADER,
    DISTRIBUTOR
}

enum class PaymentMode {
    HAND_CASH,
    BANK_TRANSFER,
    UPI_TRANSFER,
    NOT_PAID
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String, // "ADMIN", "TEAM_LEADER", "DISTRIBUTOR"
    val teamId: String,
    val teamName: String,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val leaderId: String,
    val leaderName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val teamId: String,
    val date: String, // YYYY-MM-DD
    val morningPlanning: Boolean = false,
    val adPostingsDone: Boolean = false,
    val twentyCallsDone: Boolean = false,
    val guestFollowUpsDone: Boolean = false,
    val eveningCheckoutStatus: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val eveningCheckoutTime: String = "",
    val eveningNotes: String = "",
    val customTasksJson: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_posts")
data class AdPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val teamId: String,
    val date: String, // YYYY-MM-DD
    val sourceIndex: Int, // 1 to 5
    val sourceName: String, // WhatsApp, Instagram, Facebook, Telegram, LinkedIn, etc.
    val postDetails: String = "",
    val postTime: String = "",
    val isPosted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val teamId: String,
    val date: String, // YYYY-MM-DD
    val callNumber: Int, // 1 to 20
    val contactName: String = "",
    val phoneNumber: String = "",
    val callTime: String = "",
    val callResponse: String = "Ringing", // Ringing, Callback, Interested, Confirmed Visit, Not Interested
    val isConfirmed: Boolean = false,
    val confirmationDate: String = "",
    val scheduledVisitDate: String = "",
    val scheduledVisitTime: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "guest_pipeline")
data class GuestPipelineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guestName: String,
    val phoneNumber: String,
    val distributorId: String,
    val distributorName: String,
    val teamId: String,
    val scheduledVisitDate: String,
    val callLogId: Long? = null,
    
    // Step 6: Office Arrival & File Processing (₹150)
    val hasArrived: Boolean = false,
    val arrivalTime: String = "",
    val fileProcessed: Boolean = false,
    val fileProcessedBy: String = "", // MANDATORY
    val registrationFeePaid: Boolean = false,
    val registrationPaymentMode: String = "NOT_PAID", // HAND_CASH, BANK_TRANSFER, UPI_TRANSFER, NOT_PAID
    val registrationTxnRef: String = "", // Bank or UPI reference number
    val registrationTimestamp: String = "",
    val registrationRefunded: Boolean = false,
    val registrationRefundReason: String = "",
    val registrationRefundTimestamp: String = "",
    
    // Step 7: L2 Counselling (Max 7 limit rule & manual assignment)
    val counsellingStarted: Boolean = false,
    val counsellorId: String = "",
    val counsellorName: String = "",
    val counsellingStartTime: String = "",
    val counsellingCompleted: Boolean = false,
    val counsellingNotes: String = "",
    
    // Step 8: Seniority Procedure, Multi-stage Payments, Voucher Refund & Temp ID
    val seniorityCompleted: Boolean = false,
    val seniorityAmount: Double = 0.0, // Cumulative total seniority paid
    val seniorityPaymentMode: String = "", // Latest mode
    val seniorityTxnRef: String = "",
    val seniorityDateTime: String = "",
    val temporaryId: String = "", // e.g., SG-TMP-2026-0412
    val seniorityPaymentsJson: String = "[]", // Serialized JSON array of SeniorityPaymentItem
    val seniorityRefunded: Boolean = false,
    val seniorityRefundAmount: Double = 0.0,
    val seniorityRefundReason: String = "",
    val seniorityRefundDateTime: String = "",
    val seniorityVoucherCollected: Boolean = false,
    val seniorityVoucherNumber: String = "",
    
    // Step 9: L3 Sales Follow-up (Strictly 3 times maximum)
    val followUpCount: Int = 0,
    val followUp1Date: String = "",
    val followUp1Notes: String = "",
    val followUp2Date: String = "",
    val followUp2Notes: String = "",
    val followUp3Date: String = "",
    val followUp3Notes: String = "",
    val finalOutcome: String = "PENDING", // PENDING, SALE, NOT_SALE
    val notSaleReason: String = "",
    
    // Step 10: Sales Closing
    val salesClosed: Boolean = false,
    val totalPackageAmount: Double = 0.0,
    val finalPaymentMode: String = "", // HAND_CASH, BANK_TRANSFER, UPI_TRANSFER
    val finalTxnRef: String = "",
    val salesClosingDateTime: String = "",
    val salesClosingNotes: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SeniorityPaymentItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val paymentMode: String,
    val txnRef: String,
    val timestamp: String,
    val notes: String = ""
)

fun parseSeniorityPaymentsJson(jsonStr: String): List<SeniorityPaymentItem> {
    if (jsonStr.isBlank()) return emptyList()
    return try {
        val array = org.json.JSONArray(jsonStr)
        val list = mutableListOf<SeniorityPaymentItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SeniorityPaymentItem(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    amount = obj.optDouble("amount", 0.0),
                    paymentMode = obj.optString("paymentMode", "UPI_TRANSFER"),
                    txnRef = obj.optString("txnRef", ""),
                    timestamp = obj.optString("timestamp", ""),
                    notes = obj.optString("notes", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

fun serializeSeniorityPaymentsJson(list: List<SeniorityPaymentItem>): String {
    val array = org.json.JSONArray()
    for (item in list) {
        val obj = org.json.JSONObject()
        obj.put("id", item.id)
        obj.put("amount", item.amount)
        obj.put("paymentMode", item.paymentMode)
        obj.put("txnRef", item.txnRef)
        obj.put("timestamp", item.timestamp)
        obj.put("notes", item.notes)
        array.put(obj)
    }
    return array.toString()
}


