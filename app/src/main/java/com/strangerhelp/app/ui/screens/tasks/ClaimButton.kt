package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.model.ClaimedUser

@Composable
fun ClaimButton(
    state: TaskDetailViewModel.ClaimButtonState,
    taskBudget: Int,
    maxClaimers: Int,
    claimedUsers: List<ClaimedUser>?,
    onClaimClick: () -> Unit
) {
    val Primary = Color(0xFFFFB340) // AccentOrange
    val OnPrimary = Color.White
    val Warning = Color(0xFFFFA000)
    val TrustColor = Color(0xFF00C853)

    val (text, colors, enabled) = when (state) {
        TaskDetailViewModel.ClaimButtonState.CAN_CLAIM -> {
            val slotsLeft = (maxClaimers - (claimedUsers?.size ?: 0))
            val label = if (maxClaimers > 1) {
                "Request to Join ($slotsLeft spots left)"
            } else {
                "Request to Claim"
            }
            Triple(
                label,
                ButtonDefaults.buttonColors(containerColor = Primary),
                true
            )
        }
        TaskDetailViewModel.ClaimButtonState.SENDING -> {
            Triple(
                "Sending request...",
                ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.7f)),
                false
            )
        }
        TaskDetailViewModel.ClaimButtonState.REQUESTED -> {
            Triple(
                "⏳ Request Sent — Waiting for Approval",
                ButtonDefaults.buttonColors(containerColor = Warning.copy(alpha = 0.12f)),
                false
            )
        }
        TaskDetailViewModel.ClaimButtonState.CLAIMED -> {
            val label = if (maxClaimers > 1) {
                "✓ Joined (${claimedUsers?.size ?: 0}/$maxClaimers)"
            } else {
                "✓ Claimed"
            }
            Triple(
                label,
                ButtonDefaults.buttonColors(containerColor = TrustColor.copy(alpha = 0.12f)),
                false
            )
        }
        TaskDetailViewModel.ClaimButtonState.REJECTED -> {
            Triple(
                "🔄 Request Again",
                ButtonDefaults.buttonColors(containerColor = Primary),
                true
            )
        }
        TaskDetailViewModel.ClaimButtonState.JOINED -> {
            Triple(
                "✓ Joined (${claimedUsers?.size ?: 0}/$maxClaimers)",
                ButtonDefaults.buttonColors(containerColor = TrustColor.copy(alpha = 0.12f)),
                false
            )
        }
    }

    Button(
        onClick = {
            if (state == TaskDetailViewModel.ClaimButtonState.CAN_CLAIM || state == TaskDetailViewModel.ClaimButtonState.REJECTED) {
                onClaimClick()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(26.dp)
    ) {
        if (state == TaskDetailViewModel.ClaimButtonState.SENDING) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = OnPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = when (state) {
                TaskDetailViewModel.ClaimButtonState.REQUESTED -> Warning
                TaskDetailViewModel.ClaimButtonState.CLAIMED, TaskDetailViewModel.ClaimButtonState.JOINED -> TrustColor
                else -> OnPrimary
            }
        )
    }
}
