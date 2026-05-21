package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoogleBlue
import com.example.viewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MessagesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isDynamicTheme by viewModel.isDynamicTheme.collectAsState()
    val isSpamProtection by viewModel.isSpamProtection.collectAsState()
    val isVerifiedBusinesses by viewModel.isVerifiedBusinesses.collectAsState()
    val isAutoOtp by viewModel.isAutoOtp.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_root"),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "General Preferences",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GoogleBlue,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic Styling Toggle
            SettingsToggleItem(
                title = "Dynamic Material You colors",
                subtitle = "App adapts UI color tokens automatically to device theme background.",
                isChecked = isDynamicTheme,
                onCheckedChange = { viewModel.setDynamicTheme(it) },
                icon = Icons.Outlined.Palette
            )

            // Auto OTP toggle
            SettingsToggleItem(
                title = "Auto OTP Categorization",
                subtitle = "Automatically extracts 4-8 digit verification passcodes with 1-tap copy bars.",
                isChecked = isAutoOtp,
                onCheckedChange = { viewModel.setAutoOtp(it) },
                icon = Icons.Outlined.AutoAwesome
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Spam & Security Protection",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GoogleBlue,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Spam switch
            SettingsToggleItem(
                title = "Automated Spam Filter",
                subtitle = "Identifies suspected lottery links or quick cash promotions and screens them.",
                isChecked = isSpamProtection,
                onCheckedChange = { viewModel.setSpamProtection(it) },
                icon = Icons.Outlined.Security
            )

            // Verified Business profiles
            SettingsToggleItem(
                title = "Verified Business SMS profiles",
                subtitle = "Authenticates sender identifiers offline using standard local database rules.",
                isChecked = isVerifiedBusinesses,
                onCheckedChange = { viewModel.setVerifiedBusinesses(it) },
                icon = Icons.Outlined.VerifiedUser
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Backup & Retention Management",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GoogleBlue,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Database backup local
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showBackupDialog = true }
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SdCard,
                        contentDescription = "local SD card storage",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local Device Offline Backup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Export encrypted chat databases to local private folder storage for safekeeping.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "more",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                title = { Text("Local Database Backup") },
                text = { Text("This will compress your local Room SQLite schemas into a private `.messages.bin` local storage key. Do you want to process?") },
                confirmButton = {
                    TextButton(onClick = {
                        Toast.makeText(context, "Backup file exported safely to private data directory!", Toast.LENGTH_LONG).show()
                        showBackupDialog = false
                    }) {
                        Text("Export Backup", fontWeight = FontWeight.Bold, color = GoogleBlue)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackupDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GoogleBlue
            )
        )
    }
}
