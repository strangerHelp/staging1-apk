package com.strangerhelp.app.ui.screens.ask

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.Answer
import com.strangerhelp.app.data.model.Question
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.screens.tasks.LoginPrompt
import com.strangerhelp.app.ui.theme.Body
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.OnPrimary
import com.strangerhelp.app.ui.theme.SurfaceVariant
import com.strangerhelp.app.ui.theme.Warning
import com.strangerhelp.app.ui.theme.Error
import com.strangerhelp.app.ui.theme.TrustColor
import com.strangerhelp.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskListScreen(
    viewModel: AskViewModel,
    navController: NavController
) {
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf("All", "General", "Government", "Weather", "Traffic", "Events", "Other")

    LaunchedEffect(Unit) {
        viewModel.loadQuestions(selectedCategory)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask the Community") },
                actions = {
                    IconButton(onClick = { navController.navigate("ask_post") }) {
                        Icon(Icons.Default.Add, "Ask a question")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category) }
                    )
                }
            }

            if (isLoading && questions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (questions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❓", fontSize = 48.sp)
                        Text(
                            "No questions yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Be the first to ask something!",
                            fontSize = 13.sp,
                            color = Muted
                        )
                        Button(
                            onClick = { navController.navigate("ask_post") },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Ask a Question")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(questions, key = { it.id }) { question ->
                        QuestionCard(
                            question = question,
                            onClick = {
                                navController.navigate("ask_detail/${question.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChip(
                    label = question.category,
                    containerColor = SurfaceVariant
                )
                if (question.anonymous == 1) {
                    CategoryChip(
                        label = "🕵️ Anonymous",
                        containerColor = Warning.copy(alpha = 0.12f),
                        textColor = Warning
                    )
                }
            }

            Text(
                text = question.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            question.location?.let {
                Text(
                    text = "📍 $it",
                    fontSize = 12.sp,
                    color = Muted
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (question.anonymous == 1) "Anonymous" else question.authorName,
                    fontSize = 12.sp,
                    color = Body
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👍 ${question.votes}",
                        fontSize = 12.sp,
                        color = Muted
                    )
                    Text(
                        text = "💬 ${question.answerCount}",
                        fontSize = 12.sp,
                        color = Muted
                    )
                    Text(
                        text = TimeUtils.getTimeAgo(question.createdAt),
                        fontSize = 11.sp,
                        color = Muted
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionDetailCard(question: Question) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryChip(label = question.category, containerColor = SurfaceVariant)
            if (question.anonymous == 1) {
                CategoryChip(label = "🕵️ Anonymous", containerColor = Warning.copy(alpha = 0.12f), textColor = Warning)
            }
        }
        
        Text(text = question.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        
        question.location?.let {
            Text(text = "📍 $it", fontSize = 14.sp, color = Muted)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Asked by ${if (question.anonymous == 1) "Anonymous" else question.authorName}",
                fontSize = 13.sp,
                color = Body
            )
            Text(text = TimeUtils.getTimeAgo(question.createdAt), fontSize = 12.sp, color = Muted)
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskPostScreen(
    viewModel: AskViewModel,
    navController: NavController
) {
    var text by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var location by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()

    val categories = listOf("General", "Government", "Weather", "Traffic", "Events", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask a Question") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Your Question *") },
                    placeholder = { Text("Type your question...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    isError = text.isBlank()
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (optional)") },
                    placeholder = { Text("Enter location...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🕵️ Post Anonymously", fontSize = 14.sp)
                    Switch(
                        checked = anonymous,
                        onCheckedChange = { anonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Primary,
                            checkedTrackColor = Primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            if (error != null) {
                item {
                    ErrorCard(
                        message = error ?: "",
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.postQuestion(
                            text = text,
                            category = category,
                            location = location,
                            anonymous = anonymous
                        ) {
                            navController.popBackStack()
                        }
                    },
                    enabled = text.isNotBlank() && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Post Question", color = OnPrimary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskDetailScreen(
    questionId: String,
    viewModel: AskViewModel,
    navController: NavController
) {
    val question by viewModel.selectedQuestion.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var answerText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(questionId) {
        viewModel.loadQuestion(questionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    question?.let { q ->
                        if (currentUser?.id == q.authorId) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Error)
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && question == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            question?.let { q ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        QuestionDetailCard(question = q)
                    }

                    item {
                        Text(
                            text = "💬 ${q.answers.size} Answers",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(q.answers, key = { it.id }) { answer ->
                        AnswerItem(
                            answer = answer,
                            currentUser = currentUser,
                            onVote = { vote ->
                                viewModel.voteAnswer(q.id, vote) { }
                            }
                        )
                    }

                    if (currentUser != null) {
                        item {
                            PostAnswerSection(
                                text = answerText,
                                onTextChange = { answerText = it },
                                onSubmit = {
                                    viewModel.postAnswer(q.id, answerText) {
                                        answerText = ""
                                    }
                                },
                                isSubmitting = isSubmitting
                            )
                        }
                    } else {
                        item {
                            LoginPrompt(navController = navController)
                        }
                    }

                    if (error != null) {
                        item {
                            ErrorCard(
                                message = error ?: "",
                                onDismiss = { viewModel.clearError() }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Question") },
            text = { Text("Are you sure you want to delete this question?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteQuestion(questionId) {
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AnswerItem(
    answer: Answer,
    currentUser: User?,
    onVote: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onVote("up") },
                    enabled = currentUser != null,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.ThumbUp,
                        contentDescription = "Upvote",
                        modifier = Modifier.size(16.dp),
                        tint = if (answer.votes > 0) TrustColor else Muted
                    )
                }
                Text(
                    text = answer.votes.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onVote("down") },
                    enabled = currentUser != null,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.ThumbDown,
                        contentDescription = "Downvote",
                        modifier = Modifier.size(16.dp),
                        tint = if (answer.votes < 0) Error else Muted
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = answer.authorName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Body
                    )
                    Text(
                        text = TimeUtils.getTimeAgo(answer.createdAt),
                        fontSize = 10.sp,
                        color = Muted
                    )
                }

                Text(
                    text = answer.text,
                    fontSize = 14.sp,
                    color = Body
                )
            }
        }
    }
}

@Composable
fun PostAnswerSection(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "💬 Your Answer",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Write your answer...", color = Muted) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 80.dp),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = onSubmit,
                enabled = text.isNotBlank() && !isSubmitting,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Post", color = OnPrimary)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color = com.strangerhelp.app.ui.theme.SurfaceVariant,
    textColor: androidx.compose.ui.graphics.Color = com.strangerhelp.app.ui.theme.Body
) {
    androidx.compose.material3.Surface(
        color = containerColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        androidx.compose.material3.Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = textColor,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = com.strangerhelp.app.ui.theme.Error.copy(alpha = 0.12f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.material3.Text(
                text = message,
                fontSize = 13.sp,
                color = com.strangerhelp.app.ui.theme.Error,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Delete, // Using Delete as close for simplicity or add Close
                    contentDescription = "Dismiss",
                    tint = com.strangerhelp.app.ui.theme.Error
                )
            }
        }
    }
}
