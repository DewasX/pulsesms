package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ConversationEntity
import com.example.ui.theme.GoogleBlue
import com.example.viewmodel.MessagesViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: MessagesViewModel,
    onNavigateToChat: (Int) -> Unit,
    onNavigateToContactPicker: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchived: () -> Unit,
    onNavigateToSpam: () -> Unit,
    onNavigateToScheduled: () -> Unit,
    onNavigateToLinkedDevices: () -> Unit,
    onNavigateToPromo: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val conversations by viewModel.activeConversations.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    var showProfileDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Filtered conversations list based on top pills
    val filteredConversations = remember(conversations, selectedCategory) {
        if (selectedCategory == "All") {
            conversations
        } else {
            // Check if last message corresponds to categorized queries or parse it
            conversations.filter { conv ->
                val cls = viewModel.repository.scanMessageAndClassify(conv.lastMessage)
                cls.category.uppercase() == selectedCategory.uppercase()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inbox_screen_root"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Elegant Google-styled top Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onNavigateToSearch() }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search messages, links & OTPs...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                    
                    // User Profile circle avatar in Professional Polish format
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFA8C7FA))
                            .clickable { showProfileDialog = true }
                            .testTag("profile_avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            color = Color(0xFF062E6F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToContactPicker,
                containerColor = Color(0xFFD3E3FD),
                contentColor = Color(0xFF041E49),
                icon = { Icon(Icons.Default.Chat, contentDescription = "Start chat") },
                text = { Text("Start chat", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("start_chat_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal categories row
            CategoriesRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )

            if (filteredConversations.isEmpty()) {
                // Polished Empty State layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = "Empty conversations",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No private chats yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap 'Start chat' below to connect offline.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            } else {
                // Conversation threads details list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        SwipeableConversationItem(
                            conversation = conv,
                            onClicked = { onNavigateToChat(conv.id) },
                            onPinnedToggled = { viewModel.togglePinConversation(conv.id) },
                            onArchiveToggled = {
                                viewModel.toggleArchiveConversation(conv.id)
                            },
                            onSpamToggled = {
                                viewModel.toggleSpamConversation(conv.id)
                            }
                        )
                    }
                }
            }
        }

        // Profile Dialog detailing full Google Messages style menu options
        if (showProfileDialog) {
            ProfileMenuDialog(
                onDismiss = { showProfileDialog = false },
                onNavigateToSettings = {
                    showProfileDialog = false
                    onNavigateToSettings()
                },
                onNavigateToArchived = {
                    showProfileDialog = false
                    onNavigateToArchived()
                },
                onNavigateToSpam = {
                    showProfileDialog = false
                    onNavigateToSpam()
                },
                onNavigateToScheduled = {
                    showProfileDialog = false
                    onNavigateToScheduled()
                },
                onNavigateToLinkedDevices = {
                    showProfileDialog = false
                    onNavigateToLinkedDevices()
                },
                onNavigateToPromo = {
                    showProfileDialog = false
                    onNavigateToPromo()
                }
            )
        }
    }
}

@Composable
fun CategoriesRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Personal", "Transactions", "OTPs")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            CategoryPill(
                label = cat,
                isSelected = selectedCategory == cat,
                onClick = { onCategorySelected(cat) }
            )
        }
    }
}

@Composable
fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val backgroundColor = if (isSelected) {
        if (isDark) Color(0xFF004A77) else Color(0xFFD3E3FD)
    } else {
        if (isDark) Color(0xFF1E1F22) else Color.White
    }
    
    val textColor = if (isSelected) {
        if (isDark) Color.White else Color(0xFF001D35)
    } else {
        if (isDark) Color(0xFFC4C7C5) else Color(0xFF44474E)
    }
    
    val borderColor = if (isSelected) {
        if (isDark) Color(0xFFA8C7FA).copy(alpha = 0.2f) else Color(0xFF0B57D0).copy(alpha = 0.1f)
    } else {
        if (isDark) Color(0xFF44474E) else Color(0xFFC4C7C5)
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun SwipeableConversationItem(
    conversation: ConversationEntity,
    onClicked: () -> Unit,
    onPinnedToggled: () -> Unit,
    onArchiveToggled: () -> Unit,
    onSpamToggled: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    val animatedOffset by animateDpAsState(targetValue = offsetX.dp, label = "Slide")
    var showOptionsSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 150f) {
                            onArchiveToggled()
                        } else if (offsetX < -150f) {
                            showOptionsSheet = true
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount * 0.5f // Dampen the drag to avoid snapping layout
                    }
                )
            }
            .offset { IntOffset(animatedOffset.value.roundToInt(), 0) }
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClicked() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom high contrast rounded contact badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(conversation.avatarColorHex))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.contactName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.contactName,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = formatTimestamp(conversation.lastMessageTimestamp),
                        fontSize = 12.sp,
                        color = if (conversation.unreadCount > 0) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        fontSize = 14.sp,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned thread",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(GoogleBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showOptionsSheet) {
        AlertDialog(
            onDismissRequest = { 
                showOptionsSheet = false
                offsetX = 0f
            },
            title = { Text(conversation.contactName) },
            text = { Text("What would you like to do with this conversation thread?") },
            confirmButton = {
                TextButton(onClick = {
                    onPinnedToggled()
                    showOptionsSheet = false
                }) {
                    Text(if (conversation.isPinned) "Unpin" else "Pin Thread")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onSpamToggled()
                        showOptionsSheet = false
                    }) {
                        Text("Spam", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        onArchiveToggled()
                        showOptionsSheet = false
                    }) {
                        Text("Archive")
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileMenuDialog(
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchived: () -> Unit,
    onNavigateToSpam: () -> Unit,
    onNavigateToScheduled: () -> Unit,
    onNavigateToLinkedDevices: () -> Unit,
    onNavigateToPromo: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // User info header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GoogleBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Dewas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("dewasbiz@gmail.com", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                // Menu items
                ProfileMenuItem(
                    icon = Icons.Outlined.Archive,
                    label = "Archived conversations",
                    onClickValue = onNavigateToArchived
                )

                ProfileMenuItem(
                    icon = Icons.Outlined.Report,
                    label = "Spam & blocked",
                    onClickValue = onNavigateToSpam
                )

                ProfileMenuItem(
                    icon = Icons.Outlined.Schedule,
                    label = "Scheduled queue",
                    onClickValue = onNavigateToScheduled
                )

                ProfileMenuItem(
                    icon = Icons.Outlined.QrCodeScanner,
                    label = "Device pairing (Companion)",
                    onClickValue = onNavigateToLinkedDevices
                )

                ProfileMenuItem(
                    icon = Icons.Outlined.Collections,
                    label = "Play Store Promo covers",
                    onClickValue = onNavigateToPromo
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuItem(
                    icon = Icons.Outlined.Settings,
                    label = "Messages settings",
                    onClickValue = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClickValue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClickValue() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val checkDate = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    return if (now.get(Calendar.DATE) == checkDate.get(Calendar.DATE)) {
        sdf.format(date)
    } else if (now.get(Calendar.DATE) - checkDate.get(Calendar.DATE) == 1) {
        "Yesterday"
    } else {
        val fullSdf = SimpleDateFormat("MMM d", Locale.getDefault())
        fullSdf.format(date)
    }
}
