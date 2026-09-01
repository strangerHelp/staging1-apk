package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.model.Review
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.ui.theme.*
import com.strangerhelp.app.utils.TimeUtils
import kotlin.math.floor

@Composable
fun RatingComponent(
    task: Task,
    viewModel: ReviewViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val submittedReview by viewModel.submittedReview.collectAsState()

    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    if (submittedReview != null) {
        AlreadyReviewedCard(
            review = submittedReview!!,
            modifier = modifier
        )
        return
    }

    val (revieweeId, revieweeName) = remember(task, currentUser) {
        if (currentUser != null) {
            viewModel.getRevieweeForTask(task, currentUser!!)
        } else {
            "" to "User"
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⭐ Rate $revieweeName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                (1..5).forEach { i ->
                    IconButton(
                        onClick = { rating = i },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "$i stars",
                            tint = if (i <= rating) Warning else Hairline,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            if (rating > 0) {
                Text(
                    text = when (rating) {
                        1 -> "Poor"
                        2 -> "Fair"
                        3 -> "Good"
                        4 -> "Very Good"
                        5 -> "Excellent!"
                        else -> ""
                    },
                    fontSize = 12.sp,
                    color = Muted
                )
            } else {
                Text(
                    "Tap a star to rate",
                    fontSize = 12.sp,
                    color = Muted
                )
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Leave a comment (optional)", color = Muted) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            if (error != null) {
                Text(
                    text = error ?: "",
                    fontSize = 12.sp,
                    color = Error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (rating > 0 && revieweeId.isNotEmpty()) {
                        viewModel.submitReview(
                            taskId = task._id,
                            revieweeId = revieweeId,
                            rating = rating,
                            comment = comment,
                            onSuccess = {  }
                        )
                    }
                },
                enabled = rating > 0 && !isSubmitting && revieweeId.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Review", color = OnPrimary)
                }
            }
        }
    }
}

@Composable
fun AlreadyReviewedCard(review: Review, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "You rated ",
                    fontSize = 13.sp,
                    color = Body
                )
                repeat(5) { i ->
                    Icon(
                        if (i < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (i < review.rating) Warning else Hairline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (review.comment.isNotBlank()) {
                Text(
                    text = "\"${review.comment}\"",
                    fontSize = 13.sp,
                    color = Body,
                    fontStyle = FontStyle.Italic
                )
            }

            Text(
                text = TimeUtils.getTimeAgo(review.createdAt),
                fontSize = 11.sp,
                color = Muted
            )
        }
    }
}

@Composable
fun ProfileRating(
    rating: Double,
    totalReviews: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StarRow(rating = rating)

        if (totalReviews > 0) {
            Text(
                text = "${"%.1f".format(rating)} ($totalReviews reviews)",
                fontSize = 13.sp,
                color = Body
            )
        } else {
            Text(
                text = "No reviews yet",
                fontSize = 13.sp,
                color = Muted
            )
        }
    }
}

@Composable
fun StarRow(rating: Double) {
    Row {
        (1..5).forEach { i ->
            val starType = when {
                i <= floor(rating) -> Icons.Filled.Star
                i - 0.5 <= rating && rating % 1 >= 0.5 -> Icons.Filled.StarHalf
                else -> Icons.Outlined.Star
            }
            Icon(
                starType,
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ReviewList(
    reviews: List<Review>,
    modifier: Modifier = Modifier
) {
    if (reviews.isEmpty()) {
        Text(
            "No reviews yet",
            fontSize = 13.sp,
            color = Muted,
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }

    Column(modifier = modifier) {
        reviews.forEachIndexed { index, review ->
            ReviewItem(review = review)
            if (index < reviews.size - 1) {
                HorizontalDivider(color = Hairline)
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = review.reviewerName,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Primary
            )
            Row {
                repeat(5) { i ->
                    Icon(
                        if (i < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (i < review.rating) Warning else Hairline,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = Body
            )
        }

        Text(
            text = TimeUtils.getTimeAgo(review.createdAt),
            fontSize = 11.sp,
            color = Muted
        )
    }
}
