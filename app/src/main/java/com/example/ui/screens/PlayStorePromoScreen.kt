package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoogleBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStorePromoScreen(
    onNavigateBack: () -> Unit
) {
    var selectedSlide by remember { mutableStateOf(0) }
    var showPlayStoreOverlay by remember { mutableStateOf(true) }
    var useDarkThemeVariant by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val backgroundAnim by animateColorAsState(
        targetValue = if (useDarkThemeVariant) Color(0xFF0F0F11) else Color(0xFFF4F6F9),
        animationSpec = tween(durationMillis = 500),
        label = "bgColorAnim"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("play_store_promo_root"),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text("Promo Graphic Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Play Store Asset Configurator", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { useDarkThemeVariant = !useDarkThemeVariant }) {
                        Icon(
                            imageVector = if (useDarkThemeVariant) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle visual theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundAnim)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High-fidelity Description Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (useDarkThemeVariant) Color(0xFF1E1E22) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GoogleBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Store assets",
                            tint = GoogleBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Play Store Store Feature Listing",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (useDarkThemeVariant) Color.White else Color(0xFF1B1B1F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Premium, vector-drawn 1024x500 high-conversion promotional covers. Switch tabs to preview layout safe-zones.",
                            fontSize = 12.sp,
                            color = if (useDarkThemeVariant) Color(0xFFC4C7C5) else Color(0xFF44474E),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Carousel Tab Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("1. Speed Hero", "2. Smart Schedule", "3. Spam Shield")
                tabs.forEachIndexed { index, title ->
                    val active = selectedSlide == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (active) GoogleBlue else (if (useDarkThemeVariant) Color(0xFF1E1E22) else Color(0xFFE3E3E3))
                            )
                            .clickable { selectedSlide = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) Color.White else (if (useDarkThemeVariant) Color(0xFFC4C7C5) else Color(0xFF44474E))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RENDER 1024x500 PLAY STORE BANNER (Aspect ratio 1024:500 = 2.048:1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1024f / 500f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (useDarkThemeVariant) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .shadow(8.dp)
                    .testTag("feature_image_container")
            ) {
                // Background & Artwork render dynamically
                when (selectedSlide) {
                    0 -> RenderHeroBanner(useDarkThemeVariant)
                    1 -> RenderScheduleBanner(useDarkThemeVariant)
                    2 -> RenderSpamBanner(useDarkThemeVariant)
                }

                // Play Store Simulator Overlay Frame
                if (showPlayStoreOverlay) {
                    PlayStoreOverlaySimulation(useDarkThemeVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Switches to control configuration preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "overlay toggle",
                        tint = if (useDarkThemeVariant) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulate Google Play Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (useDarkThemeVariant) Color.White else Color.Black
                    )
                }
                Switch(
                    checked = showPlayStoreOverlay,
                    onCheckedChange = { showPlayStoreOverlay = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = GoogleBlue)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details and Specifications of the graphic cover
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (useDarkThemeVariant) Color(0xFF1E1E22) else Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Visual Asset Specifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (useDarkThemeVariant) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val specTitle = when (selectedSlide) {
                        0 -> "Pulse SMS Brand Identity & Connection Hero"
                        1 -> "Time-Shifted SMS & Automated Transmission"
                        else -> "On-Device Spam Defense & Real-Time Parsing"
                    }

                    val specDetails = when (selectedSlide) {
                        0 -> "Features active asymmetric dynamic laser pulse visual beams. The core logo floats with glowing ambient shadows in high-contrast light colors for crisp legibility inside recommended Play Store grids."
                        1 -> "Demonstrates high-speed scheduler calendars and cross-device syncing indicators. Visually portrays how messages wait in scheduled queue states and deliver seamlessly right on time."
                        else -> "Represents high-security system defense using deep warning colors, shield graphics, and custom anti-fraud filter blocks protecting text flow integrity."
                    }

                    SpecRow("Aspect Target", "1024 x 500 pixels (Landscape Banner)", useDarkThemeVariant)
                    SpecRow("Aesthetic Style", "Premium Dark Tech / Glassmorphic", useDarkThemeVariant)
                    SpecRow("Core Concept", specTitle, useDarkThemeVariant)
                    SpecRow("Core Palette", if (selectedSlide == 0) "Google Blue, Cyan Pulse, Charcoal" else if (selectedSlide == 1) "Warm Indigo, Magenta, Dark Slate" else "Electric Teal, Coral Red, Dark Grey", useDarkThemeVariant)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = if (useDarkThemeVariant) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = specDetails,
                        fontSize = 12.sp,
                        color = if (useDarkThemeVariant) Color(0xFFC4C7C5) else Color(0xFF44474E),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, valStr: String, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isDark) Color(0xFF8E918F) else Color(0xFF74777F),
            fontWeight = FontWeight.Normal
        )
        Text(
            text = valStr,
            fontSize = 12.sp,
            color = if (isDark) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BoxScope.RenderHeroBanner(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val gradientBg = if (isDark) {
        Brush.radialGradient(
            colors = listOf(Color(0xFF1E2845), Color(0xFF0F0F12)),
            center = Offset(700f, 250f)
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFFE2EDFE), Color(0xFFF0F4F8)),
            center = Offset(700f, 250f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
    ) {
        // Draw elegant glowing custom line waves using dynamic canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            val baseLine = h * 0.5f

            // Drawing pulse curves
            drawCircle(
                color = Color(0xFF0B57D0).copy(alpha = 0.08f * pulseScale),
                radius = 320f * pulseScale,
                center = Offset(w * 0.72f, h * 0.5f)
            )

            drawCircle(
                color = Color(0xFFA8C7FA).copy(alpha = 0.04f),
                radius = 480f,
                center = Offset(w * 0.72f, h * 0.5f)
            )
        }

        // Beautiful contents row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Text branding left side
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0B57D0).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AISTUDIO PREMIUM PLATFORM",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B57D0),
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pulse SMS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1B1B1F),
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lightning-fast messaging client built fully offline-first for extreme speed.",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFC4C7C5) else Color(0xFF44474E),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureBag("Offline Sync", isDark)
                    FeatureBag("OTP Copy", isDark)
                    FeatureBag("Material You", isDark)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right side graphic mock - Premium overlapping glassmorphic chat thread UI
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-20).dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(
                            if (isDark) Color(0xFF202124).copy(alpha = 0.9f) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                        .width(170.dp)
                        .padding(10.dp)
                ) {
                    // Chat header mock
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B57D0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Alex Rivera", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                            Text("Active now", fontSize = 6.sp, color = Color.Green)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Text bubbles
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .background(Color(0xFF0B57D0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("Ready for pairing?", color = Color.White, fontSize = 7.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .background(if (isDark) Color(0xFF2D2F31) else Color(0xFFEAF1FB), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("Yes, fully synced! ⚡", color = if (isDark) Color.White else Color.Black, fontSize = 7.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.RenderScheduleBanner(isDark: Boolean) {
    val gradientBg = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF281145), Color(0xFF0A0712))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFF2E6FF), Color(0xFFF9F6FC))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF7D5260).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TIMED AUTOMATION ENGINE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7D5260),
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scheduled SMS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1B1B1F),
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Compose messages now and let our persistent background engine deliver them at the prime moment.",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFC4C7C5) else Color(0xFF44474E),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureBag("Auto Dispatch", isDark)
                    FeatureBag("Time Picker", isDark)
                    FeatureBag("Sync Queues", isDark)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Overlapping Card with interactive visual components (e.g. Schedule clock picker)
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .background(
                            if (isDark) Color(0xFF1F1B24) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF7D5260).copy(alpha = 0.3f) else Color(0xFF7D5260).copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .width(180.dp)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "timer",
                                tint = Color(0xFF7D5260),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Schedule Post", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF7D5260).copy(alpha = 0.15f), CircleShape)
                                .size(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "check", tint = Color(0xFF7D5260), modifier = Modifier.size(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Deliver to: Mom ❤️", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color(0xFFC4C7C5) else Color(0xFF44474E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF2A2433) else Color(0xFFF5EEFC), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "\"Happy Birthday! Hope you have a wonderful day! 🎉\"",
                            fontSize = 7.sp,
                            color = if (isDark) Color.White else Color.Black,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tomorrow, 8:00 AM", fontSize = 7.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D5260))
                        Text("QUEUE STATUS", fontSize = 6.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.RenderSpamBanner(isDark: Boolean) {
    val gradientBg = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF003020), Color(0xFF030A0A))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFE6F4EA), Color(0xFFF1F8F5))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF137333).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ON-DEVICE THREAT DEFENSE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF137333),
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Spam Security",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1B1B1F),
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Intelligent offline filters audit messages for risky hyperlinks and screen verified entities automatically.",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFC4C7C5) else Color(0xFF44474E),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureBag("Spam Filter", isDark)
                    FeatureBag("Phishing Block", isDark)
                    FeatureBag("Local DB", isDark)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Overlapping Card with interactive visual components - Spam shield block
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .background(
                            if (isDark) Color(0xFF141F17) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF137333).copy(alpha = 0.3f) else Color(0xFF137333).copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .width(185.dp)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = Color(0xFF137333),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Threat Blocked",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF137333)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "From: Unknown (+1-555-092-231)",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) Color(0xFF2D181A) else Color(0xFFFDF2F2),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                0.5.dp,
                                Color.Red.copy(alpha = 0.3f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "CLAIM YOUR $10K USD CASH NOW IMMEDIATELY WITH THIS UNSECURED LINK http://phish-lottery.click",
                            fontSize = 7.sp,
                            color = if (isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS: SILENTLY SCREENED",
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF137333)
                        )
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "block icon",
                            tint = Color.Red.copy(alpha = 0.6f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureBag(text: String, isDark: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 7.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color.White else Color.Black
        )
    }
}

@Composable
fun BoxScope.PlayStoreOverlaySimulation(isDark: Boolean) {
    // Bottom banner with Download/Rating overlays common in app store graphics
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                )
            )
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left details - Star metrics
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mock custom white app icon placeholder
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "mock icon",
                        tint = GoogleBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pulse SMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "star",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "4.9 ★ (10K+ installs)",
                            fontSize = 7.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right details - Google Play Badge simulation
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF01875F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GetApp,
                        contentDescription = "Install now",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "INSTALL",
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
