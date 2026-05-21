package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.ui.theme.*
import com.example.viewmodel.MessagesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: MessagesViewModel,
    conversationId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var activeConversation by remember { mutableStateOf<ConversationEntity?>(null) }
    val messages by viewModel.activeMessages.collectAsState()
    val typingStates by viewModel.typingStates.collectAsState()
    val selectedSim by viewModel.selectedSim.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showTranslationDialog by remember { mutableStateOf<MessageEntity?>(null) }
    
    val isTyping = typingStates[conversationId] ?: false

    LaunchedEffect(key1 = conversationId) {
        viewModel.selectConversation(conversationId)
        val conv = viewModel.repository.getConversationById(conversationId)
        activeConversation = conv
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("conversation_screen_root"),
        topBar = {
            activeConversation?.let { conv ->
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.selectConversation(null)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(conv.avatarColorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conv.contactName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    conv.contactName, 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    if (isTyping) "Typing..." else "SIM $selectedSim • End-to-end Encrypted",
                                    fontSize = 11.sp,
                                    color = if (isTyping) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.togglePinConversation(conversationId)
                            Toast.makeText(context, if (conv.isPinned) "Unpinned chat" else "Pinned chat", Toast.LENGTH_SHORT).show()
                            activeConversation = conv.copy(isPinned = !conv.isPinned)
                        }) {
                            Icon(
                                imageVector = if (conv.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                                contentDescription = "Pin thread",
                                tint = if (conv.isPinned) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            viewModel.toggleArchiveConversation(conversationId)
                            Toast.makeText(context, "Thread Archived", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive thread")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Spam protection center banner
            if (activeConversation?.isSpam == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Spam warning",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Flagged as Spam protection alert.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        
                        Row {
                            TextButton(onClick = {
                                viewModel.toggleSpamConversation(conversationId)
                                Toast.makeText(context, "Conversation Restored", Toast.LENGTH_SHORT).show()
                                activeConversation = activeConversation?.copy(isSpam = false)
                            }) {
                                Text("NOT SPAM", fontSize = 12.sp, color = GoogleBlue)
                            }
                        }
                    }
                }
            }

            // Message thread lists
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                item {
                    // Chat security header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted Lock",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "End-to-End Encrypted",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "This offline-first secure thread remains strictly on this device. Messages, media, and transcriptions are never decrypted.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                            lineHeight = 16.sp
                        )
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubbleItem(
                        message = msg,
                        onCopyOtp = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "OTP Code Copied: $it", Toast.LENGTH_SHORT).show()
                        },
                        onOpenTranslation = {
                            showTranslationDialog = msg
                        }
                    )
                }

                if (isTyping) {
                    item {
                        TypingBubbleIndicator()
                    }
                }
            }

            // Suggested Replies chips panel
            val smartReplies = viewModel.getSmartReplies(conversationId)
            if (smartReplies.isNotEmpty() && !isTyping) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    smartReplies.forEach { reply ->
                        SuggestionChip(
                            onClick = {
                                if (reply.startsWith("Copy code:")) {
                                    val code = reply.substringAfter("Copy code:").trim()
                                    clipboardManager.setText(AnnotatedString(code))
                                    Toast.makeText(context, "OTP Code Copied: $code", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.sendMessage(reply)
                                }
                            },
                            label = { Text(reply, fontWeight = FontWeight.Medium, fontSize = 13.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = GoogleBlue,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Dynamic voice recording visual wave simulation
            if (isRecordingVoice) {
                VoiceRecordingPanel(
                    onCancel = { viewModel.stopVoiceRecording {} },
                    onComplete = {
                        viewModel.stopVoiceRecording { transcript ->
                            inputText = transcript
                        }
                    }
                )
            } else {
                // Bottom Input frame
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SIM Dual Selector
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (selectedSim == 1) GoogleBlue.copy(alpha = 0.12f) else Color(0xFFC2185B).copy(alpha = 0.12f)
                            )
                            .clickable {
                                viewModel.selectSim(if (selectedSim == 1) 2 else 1)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SIM $selectedSim",
                            color = if (selectedSim == 1) GoogleBlue else Color(0xFFC2185B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input container resembling Google Messages exactly
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showScheduleDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Schedule sending message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Chat message...", fontSize = 15.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("msg_input_field")
                        )

                        // Voice transcribe button
                        IconButton(onClick = { viewModel.startVoiceRecording() }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice note transcribe",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button with ripple trigger
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isBlank()) MaterialTheme.colorScheme.surfaceVariant else GoogleBlue)
                            .clickable(enabled = inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                            .testTag("send_msg_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send message",
                            tint = if (inputText.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Live Translation Choice dialog
        showTranslationDialog?.let { currentMsg ->
            AlertDialog(
                onDismissRequest = { showTranslationDialog = null },
                title = { Text("Translate Message") },
                text = { Text("Instantly translate this message offline using pre-bundled local dictionaries. Choose destination language:") },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            viewModel.performLiveTranslation(currentMsg.id, "Spanish")
                            showTranslationDialog = null
                        }) {
                            Text("Spanish (ES)")
                        }
                        TextButton(onClick = {
                            viewModel.performLiveTranslation(currentMsg.id, "French")
                            showTranslationDialog = null
                        }) {
                            Text("French (FR)")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTranslationDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Custom Message Scheduling dialog layout
        if (showScheduleDialog) {
            ScheduleMessageDialog(
                onDismiss = { showScheduleDialog = false },
                onScheduleSet = { delayMinutes ->
                    val triggerTime = System.currentTimeMillis() + delayMinutes * 60 * 1000
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(
                            body = inputText,
                            isScheduled = true,
                            scheduledTime = triggerTime
                        )
                        inputText = ""
                        Toast.makeText(context, "Message scheduled successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please draft a message first to schedule", Toast.LENGTH_SHORT).show()
                    }
                    showScheduleDialog = false
                }
            )
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    onCopyOtp: (String) -> Unit,
    onOpenTranslation: () -> Unit
) {
    val isMe = message.isFromMe
    val arrangement = if (isMe) Arrangement.End else Arrangement.Start
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = arrangement
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp
                ),
                color = if (isMe) BubbleMeDark else BubbleOtherDark,
                tonalElevation = 1.dp,
                modifier = Modifier.clickable { onOpenTranslation() }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.body,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    
                    if (message.translatedText != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translated",
                                tint = GoogleBlueContainerLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.translatedText,
                                fontSize = 13.sp,
                                color = GoogleBlueContainerLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Sub-elements like transaction tags, scheduled notifications, or OTP quick copy bars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (message.isScheduled) "Scheduled" else formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                
                if (message.isScheduled) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Pending scheduled delivery",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (isMe && !message.isScheduled) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Delivered",
                        tint = GoogleBlue,
                        modifier = Modifier.size(12.dp)
                    )
                }
                
                if (!isMe && message.category != "PERSONAL") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (message.category) {
                                    "OTP" -> Color(0xFF34A853).copy(alpha = 0.15f)
                                    "TRANSACTION" -> Color(0xFFFBBC05).copy(alpha = 0.15f)
                                    else -> Color(0xFFEA4335).copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = message.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (message.category) {
                                "OTP" -> Color(0xFF228B22)
                                "TRANSACTION" -> Color(0xFFB8860B)
                                else -> Color(0xFFD2143A)
                            }
                        )
                    }
                }
            }

            // Inline OTP Copy Pill
            if (message.isOtp && message.otpCode != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { onCopyOtp(message.otpCode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34A853),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", modifier = Modifier.size(12.dp))
                        Text(text = "Copy Code: ${message.otpCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TypingBubbleIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 4.dp,
                bottomEnd = 18.dp
            ),
            color = BubbleOtherDark
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "dots")
                repeat(3) { index ->
                    val delayVal = index * 150
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 600
                                0.4f at 0
                                1.2f at 300
                                0.4f at 600
                            },
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (scale > 0.8f) 0.9f else 0.4f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceRecordingPanel(
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recording with Offline Voice Transcriber...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Realistic moving sound frequency waves representation
            Row(
                modifier = Modifier.height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val waveTransition = rememberInfiniteTransition(label = "voice")
                repeat(20) { index ->
                    val heightMultiplier = (index % 4 + 1) * 8
                    val animateHeight by waveTransition.animateFloat(
                        initialValue = 6f,
                        targetValue = heightMultiplier.toFloat() + 10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 400 + index * 20, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wave"
                    )
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(animateHeight.dp)
                            .clip(CircleShape)
                            .background(GoogleBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Done")
                    Text("Finish & Transcribe")
                }
            }
        }
    }
}

@Composable
fun ScheduleMessageDialog(
    onDismiss: () -> Unit,
    onScheduleSet: (Int) -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Schedule",
                    tint = GoogleBlue,
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Offline Queue Message",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Configure off-hour, silent scheduled local dispatch. Choose simulated delivery delay:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                ScheduleOptionButton(label = "In 1 Minute (Simulated Quick Trigger)", minutes = 1, onClick = onScheduleSet)
                ScheduleOptionButton(label = "In 10 Minutes", minutes = 10, onClick = onScheduleSet)
                ScheduleOptionButton(label = "In 1 Hour", minutes = 60, onClick = onScheduleSet)

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ScheduleOptionButton(
    label: String,
    minutes: Int,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick(minutes) }
            .padding(14.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
