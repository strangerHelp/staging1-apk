package com.strangerhelp.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.strangerhelp.app.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, user: User) {
    var name by remember { mutableStateOf(user.name) }
    var handle by remember { mutableStateOf(user.handle ?: "") }
    var bio by remember { mutableStateOf("Urban explorer and foodie.") }
    var city by remember { mutableStateOf(user.city ?: "") }
    var locality by remember { mutableStateOf("Koramangala") }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    
    val handleError = handle == "ravi_kumar" // Mock error state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F9F9))
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF9F9F9)).padding(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(top = 8.dp)) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${user.name}&background=E0E0E0&color=333&size=200",
                    contentDescription = "Avatar",
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("CHANGE AVATAR", color = Color(0xFFF57C00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(32.dp))
            
            // Personal Information
            SectionTitle("Personal Information")
            CustomTextField(label = "FULL NAME", value = name, onValueChange = { name = it })
            
            Spacer(Modifier.height(16.dp))
            CustomTextField(
                label = "USERNAME", 
                value = "@$handle", 
                onValueChange = { handle = it.removePrefix("@") },
                isError = handleError,
                errorMessage = "Handle already taken",
                trailingText = "3 chars min",
                trailingIcon = if (handleError) Icons.Default.ErrorOutline else null
            )
            
            Spacer(Modifier.height(16.dp))
            CustomTextField(
                label = "BIO", 
                value = bio, 
                onValueChange = { bio = it },
                singleLine = false,
                modifier = Modifier.height(100.dp),
                topRightText = "${bio.length}/5000"
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Skills
            SectionTitle("Skills")
            Text("Add skills to help others find you for specific tasks.", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth())
            
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        SkillChip("Cooking")
                        SkillChip("Driving")
                        SkillChip("Photography")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Add skill...", color = Color.Gray, fontSize = 14.sp)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text("SUGGESTED", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.DarkGray, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SuggestedSkillChip("+ Plumbing")
                SuggestedSkillChip("+ Electrical")
                SuggestedSkillChip("+ Tutoring")
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Location
            SectionTitle("Location")
            CustomTextField(label = "CITY", value = city, onValueChange = { city = it })
            Spacer(Modifier.height(16.dp))
            CustomTextField(label = "AREA / LOCALITY", value = locality, onValueChange = { locality = it })
            Spacer(Modifier.height(16.dp))
            CustomTextField(label = "COUNTRY", value = "India", onValueChange = { })
            
            Spacer(Modifier.height(32.dp))
            
            // Contact
            SectionTitle("Contact")
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("PHONE", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(48.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                    }
                    Divider(modifier = Modifier.width(1.dp).fillMaxHeight().padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
                    BasicTextFieldWrapper(value = phone, onValueChange = { phone = it }, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.height(48.dp),
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingText: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    topRightText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, letterSpacing = 1.sp, color = if (isError) Color(0xFFD32F2F) else Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
            if (topRightText != null) {
                Text(topRightText, fontSize = 10.sp, color = Color.Gray)
            }
        }
        val borderColor = if (isError) Color(0xFFD32F2F) else Color(0xFFE0E0E0)
        Row(
            modifier = Modifier.fillMaxWidth().then(modifier).background(if (isError) Color(0xFFFFEBEE) else Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, borderColor, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            BasicTextFieldWrapper(value = value, onValueChange = onValueChange, modifier = Modifier.weight(1f), singleLine = singleLine)
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (isError && errorMessage != null) {
                Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 10.sp)
            } else {
                Spacer(Modifier.width(1.dp))
            }
            if (trailingText != null) {
                Text(trailingText, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun BasicTextFieldWrapper(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, singleLine: Boolean = true) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = Color.Black)
    )
}

@Composable
fun SkillChip(label: String) {
    Row(
        modifier = Modifier.background(Color(0xFFFCE4EC), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF795548))
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFF795548), modifier = Modifier.size(12.dp))
    }
}

@Composable
fun SuggestedSkillChip(label: String) {
    Box(
        modifier = Modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp)).background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 12.sp, color = Color.DarkGray)
    }
}
