package com.strangerhelp.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.ui.theme.Body
import com.strangerhelp.app.ui.theme.Error
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.Warning

@Composable
fun EmailVerificationBanner(
    onResendClick: () -> Unit,
    isSending: Boolean,
    message: String?,
    error: String?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Warning.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Verify your email to unlock all features",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Body,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onResendClick,
                    enabled = !isSending,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Primary
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Primary
                        )
                    } else {
                        Text("Resend", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Success/Error messages
            message?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = CyanDeep,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            error?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
