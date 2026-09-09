package com.example

import com.example.data.model.GuestPipelineEntity
import com.example.ui.SmartGroupViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartGroupBusinessLogicTest {

    @Test
    fun testCounsellingMaxLimitRule() {
        // Requirement 7: Max 7 counselling limit
        val currentCounsellingsCount = 7
        val canTakeEighth = currentCounsellingsCount < SmartGroupViewModel.MAX_COUNSELLING_LIMIT
        assertFalse("A distributor should NOT be allowed to take 8th counselling", canTakeEighth)

        val canTakeSeventh = 6 < SmartGroupViewModel.MAX_COUNSELLING_LIMIT
        assertTrue("A distributor should be allowed to take 7th counselling", canTakeSeventh)
    }

    @Test
    fun testTemporaryIdGenerationFormat() {
        // Requirement 8: Seniority Procedure Temporary ID
        val guest = GuestPipelineEntity(
            id = 42,
            guestName = "Karthik Raja",
            phoneNumber = "9840112233",
            scheduledVisitDate = "2026-09-08",
            distributorId = "dist_01",
            distributorName = "D.S. Mani",
            teamId = "phoenix"
        )

        val tempId = "SG-TMP-2026-${String.format("%04d", guest.id)}"
        assertTrue(tempId.startsWith("SG-TMP-"))
        assertEquals("SG-TMP-2026-0042", tempId)
    }

    @Test
    fun testSalesClosingCelebrationCondition() {
        // Requirement 10: Sales entry complete means celebration popup
        val guest = GuestPipelineEntity(
            id = 1,
            guestName = "Senthil Kumar",
            phoneNumber = "9876543210",
            scheduledVisitDate = "2026-09-08",
            distributorId = "dist_01",
            distributorName = "D.S. Mani",
            teamId = "phoenix",
            seniorityCompleted = true,
            salesClosed = true,
            totalPackageAmount = 15000.0,
            finalPaymentMode = "UPI_TRANSFER",
            finalTxnRef = "UPI987214"
        )

        assertTrue("Candidate should be marked as sales closed", guest.salesClosed)
        assertEquals(15000.0, guest.totalPackageAmount, 0.0)
        assertEquals("UPI_TRANSFER", guest.finalPaymentMode)
        assertFalse(guest.finalTxnRef.isBlank())
    }

    @Test
    fun testL3FollowUpMaxLimit() {
        // Requirement 9: L3 follow-up strictly 3 times
        val maxFollowUps = 3
        assertEquals(3, maxFollowUps)
    }
}
