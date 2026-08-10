package com.strangerhelp.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

val OrangePrimary = Color(0xFFF5A623)
val DarkNavy = Color(0xFF101828)
val LightBeige = Color(0xFFFAF9F6)
val TextDark = Color(0xFF101828)
val MutedGray = Color(0xFF666666)
val verifiedCyan = Color(0xFFE0F7FA)
val verifiedCyanText = Color(0xFF00838F)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingScreen(onLoginClick: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBeige)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Logo",
                        tint = DarkNavy,
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Handshake,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(16.dp).padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.ui.text.buildAnnotatedString {
                    withStyle(androidx.compose.ui.text.SpanStyle(color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append("stranger")
                    }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append("help")
                    }
                }.let { text ->
                    Text(text = text)
                }
            }
            TextButton(onClick = onLoginClick) {
                Text("Login/Join", color = TextDark, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
        
        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

        // Hero Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Text(
                text = "Someone nearby can\ndo that for you",
                fontSize = 34.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Search Input Placeholder
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = border(1.dp, Color.Black.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("What do you need done, and where?", color = MutedGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("e.g. Collect dry cleaning near MG Road", color = TextDark, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Post a task", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextDark)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Map Overlay Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1524661135-423995f22d0b?q=80&w=800&auto=format&fit=crop", // generic map like image
                    contentDescription = "Map background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.6f
                )
                
                // Overlay Card
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(DarkNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Queue standing", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                            Text("650m away • ₹300", color = MutedGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = verifiedCyan,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = verifiedCyanText, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Claimed by Priya ✓ verified", color = verifiedCyanText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), thickness = 8.dp)

        // How it works
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("How it works", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(24.dp))
            
            StepItem(step = "1", title = "Post a Task", description = "Describe what you need, where, and set your price.", isLast = false)
            StepItem(step = "2", title = "Local Helper Claims", description = "A verified nearby user accepts the task instantly.", isLast = false)
            StepItem(step = "3", title = "Get Proof & Pay", description = "Receive photo confirmation before payment is released.", isLast = true)
        }

        // Categories Grid
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val itemModifier = Modifier.weight(1f)
            CategoryItem("Doc Submission", Icons.Outlined.Description, itemModifier)
            CategoryItem("Photo Verification", Icons.Outlined.VerifiedUser, itemModifier)
            CategoryItem("Parcel Pickup", Icons.Outlined.LocalShipping, itemModifier)
            CategoryItem("Queue Standing", Icons.Outlined.PersonPin, itemModifier)
            CategoryItem("Local Shopping", Icons.Outlined.ShoppingBag, itemModifier)
            CategoryItem("Form Filling", Icons.Outlined.Edit, itemModifier)
            CategoryItem("Courier Drop", Icons.Outlined.Inventory2, itemModifier)
            CategoryItem("Pet Walk", Icons.Outlined.Pets, itemModifier)
        }
        
        // Trust & Proof Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkNavy)
                .padding(24.dp)
        ) {
            Text("Trust & Proof", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=200&auto=format&fit=crop",
                            contentDescription = "Priya",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Priya S.", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Filled.Star, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                                Text(" 4.9", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = verifiedCyan, shape = RoundedCornerShape(4.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = verifiedCyanText, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified Helper", color = verifiedCyanText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?q=80&w=600&auto=format&fit=crop",
                            contentDescription = "Proof",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Proof of Submission", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        // Active Cities
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("ACTIVE CITIES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MutedGray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Delhi", "Mumbai", "Bangalore", "Chennai", "Hyderabad", "Pune", "Kolkata", "Ahmedabad").forEach { city ->
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        border = border(1.dp, Color.Black.copy(alpha = 0.15f)),
                        color = Color.Transparent
                    ) {
                        Text(city, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = TextDark, fontSize = 14.sp)
                    }
                }
            }
        }
        
        // Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkNavy)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Start Now", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Logo", tint = DarkNavy, modifier = Modifier.size(24.dp))
                        Icon(Icons.Outlined.Handshake, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(12.dp).padding(bottom = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.ui.text.buildAnnotatedString {
                        withStyle(androidx.compose.ui.text.SpanStyle(color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append("stranger")
                        }
                        withStyle(androidx.compose.ui.text.SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append("help")
                        }
                    }.let { text ->
                        Text(text = text)
                    }
                }
            }
            
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Text("Terms of Service", color = MutedGray, fontSize = 12.sp)
                Text("Privacy Policy", color = MutedGray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Text("City Guidelines", color = MutedGray, fontSize = 12.sp)
                Text("Support", color = MutedGray, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("© 2024 StrangerHelp. A Civic Utility Infrastructure.", color = MutedGray, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StepItem(step: String, title: String, description: String, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                border = border(2.dp, TextDark),
                color = if (step == "3") OrangePrimary else Color.Transparent,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(step, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                }
            }
            if (!isLast) {
                Box(modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(Color.Black.copy(alpha = 0.1f)))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = MutedGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun CategoryItem(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = border(1.dp, Color.Black.copy(alpha = 0.1f)),
        color = Color.White,
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextDark, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextDark)
        }
    }
}

fun border(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
