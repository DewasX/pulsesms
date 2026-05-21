package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.SettingsManager
import com.example.data.repository.MessageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = MessageRepository(database.messageDao)
    val settingsManager = SettingsManager(application)

    // Dynamic theming preferences
    val isDynamicTheme = settingsManager.isDynamicThemeEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val isSpamProtection = settingsManager.isSpamProtectionEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val isVerifiedBusinesses = settingsManager.isVerifiedBusinessesEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val isAutoOtp = settingsManager.isAutoOtpEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val isOnboardingCompleted = settingsManager.isOnboardingCompleted.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    // Conversations state streams
    val activeConversations = repository.activeConversations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val archivedConversations = repository.archivedConversations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val spamConversations = repository.spamConversations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Current open conversation tracking
    private val _currentConversationId = MutableStateFlow<Int?>(null)
    val currentConversationId = _currentConversationId.asStateFlow()

    // Message flow for active conversation
    val activeMessages = _currentConversationId.flatMapLatest { id ->
        if (id != null) {
            repository.getMessages(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scheduled messages total stream
    val scheduledMessages = repository.getScheduledMessages().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Typing simulated indicator states: Maps Conversation ID -> Boolean
    private val _typingStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val typingStates = _typingStates.asStateFlow()

    // Selected SIM: 1 or 2
    private val _selectedSim = MutableStateFlow(1)
    val selectedSim = _selectedSim.asStateFlow()

    // Voice recording visualizer and status simulation
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice = _isRecordingVoice.asStateFlow()

    // Live search query matching messages
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults = _searchQuery.debounce(150).flatMapLatest { q ->
        if (q.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.searchMessages(q)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize with default mock conversations
        viewModelScope.launch {
            repository.populateMockData()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsManager.setOnboardingCompleted(true)
        }
    }

    fun selectConversation(id: Int?) {
        _currentConversationId.value = id
        if (id != null) {
            viewModelScope.launch {
                val conversation = repository.getConversationById(id)
                if (conversation != null && conversation.unreadCount > 0) {
                    repository.updateConversation(conversation.copy(unreadCount = 0))
                }
            }
        }
    }

    fun selectSim(sim: Int) {
        _selectedSim.value = sim
    }

    fun setDynamicTheme(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setDynamicThemeEnabled(enabled) }
    }

    fun setSpamProtection(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setSpamProtectionEnabled(enabled) }
    }

    fun setVerifiedBusinesses(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setVerifiedBusinessesEnabled(enabled) }
    }

    fun setAutoOtp(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setAutoOtpEnabled(enabled) }
    }

    fun togglePinConversation(id: Int) {
        viewModelScope.launch {
            val conv = repository.getConversationById(id) ?: return@launch
            repository.updateConversation(conv.copy(isPinned = !conv.isPinned))
        }
    }

    fun toggleArchiveConversation(id: Int) {
        viewModelScope.launch {
            val conv = repository.getConversationById(id) ?: return@launch
            repository.updateConversation(conv.copy(isArchived = !conv.isArchived))
        }
    }

    fun toggleSpamConversation(id: Int) {
        viewModelScope.launch {
            val conv = repository.getConversationById(id) ?: return@launch
            repository.updateConversation(conv.copy(isSpam = !conv.isSpam, isArchived = false))
        }
    }

    fun createNewConversation(name: String, phone: String): Int {
        val colors = listOf("#1A73E8", "#34A853", "#EA4335", "#FBBC05", "#AB47BC", "#26C6DA")
        val randomColor = colors.random()
        
        var targetId = 0
        viewModelScope.launch {
            val existing = repository.getConversationByPhone(phone)
            if (existing != null) {
                targetId = existing.id
                selectConversation(targetId)
            } else {
                val newConv = ConversationEntity(
                    contactName = name,
                    contactPhone = phone,
                    avatarColorHex = randomColor,
                    lastMessage = "Started a chat",
                    lastMessageTimestamp = System.currentTimeMillis()
                )
                targetId = repository.insertConversation(newConv).toInt()
                selectConversation(targetId)
            }
        }
        return targetId
    }

    fun sendMessage(body: String, isScheduled: Boolean = false, scheduledTime: Long? = null) {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            val classified = repository.scanMessageAndClassify(body)
            
            val msg = MessageEntity(
                conversationId = convId,
                body = body,
                timestamp = if (isScheduled) (scheduledTime ?: System.currentTimeMillis()) else System.currentTimeMillis(),
                isFromMe = true,
                isScheduled = isScheduled,
                scheduledTime = if (isScheduled) scheduledTime else null,
                category = classified.category,
                isOtp = classified.isOtp,
                otpCode = classified.otpCode,
                status = if (isScheduled) "PENDING" else "SENT"
            )
            val msgId = repository.insertMessage(msg)

            if (!isScheduled) {
                // Trigger dynamic typing and auto reply simulation to make the UI interactive
                triggerSimulatedAutoResponse(convId, body)
            }
        }
    }

    fun triggerScheduledDelivery(msgId: Int) {
        viewModelScope.launch {
            val scheduledMsg = database.messageDao.getMessageById(msgId)
            if (scheduledMsg != null && scheduledMsg.isScheduled) {
                val updatedMsg = scheduledMsg.copy(
                    isScheduled = false,
                    status = "DELIVERED",
                    timestamp = System.currentTimeMillis()
                )
                repository.updateMessage(updatedMsg)
                
                // Update conversation details
                val conversation = repository.getConversationById(scheduledMsg.conversationId)
                if (conversation != null) {
                    repository.updateConversation(conversation.copy(
                        lastMessage = scheduledMsg.body,
                        lastMessageTimestamp = System.currentTimeMillis()
                    ))
                }
                
                // Trigger automatic response simulation if appropriate
                triggerSimulatedAutoResponse(scheduledMsg.conversationId, scheduledMsg.body)
            }
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
    }

    fun stopVoiceRecording(onTranscribed: (String) -> Unit) {
        _isRecordingVoice.value = false
        // Simulate local offline voice-to-text algorithm
        val transcriptions = listOf(
            "Hey, I am walking over to your place now!",
            "Got your message, let's meet up in 5 minutes.",
            "Can you send me the address of the restaurant?",
            "Hey there, please let me know when you get this."
        )
        onTranscribed(transcriptions.random())
    }

    fun performLiveTranslation(msgId: Int, targetLanguage: String) {
        viewModelScope.launch {
            val msg = database.messageDao.getMessageById(msgId) ?: return@launch
            // Local offline language model translation mapping
            val translated = translateTextLocally(msg.body, targetLanguage)
            repository.updateMessage(msg.copy(translatedText = translated))
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getSmartReplies(conversationId: Int): List<String> {
        val lastMsg = activeMessages.value.lastOrNull { !it.isFromMe }?.body?.lowercase() ?: ""
        return when {
            lastMsg.contains("coffee") || lastMsg.contains("cafe") -> listOf("Sure! What time?", "Yes, let's go ☕", "Sorry, busy today")
            lastMsg.contains("hey") || lastMsg.contains("hello") || lastMsg.contains("hi") -> listOf("Hey! What's up?", "Hi, how are you?", "Hello!")
            lastMsg.contains("where") -> listOf("On my way!", "At home checking", "Specify location")
            lastMsg.contains("otp") || lastMsg.contains("code") || lastMsg.contains("verification") -> {
                val code = activeMessages.value.lastOrNull { !it.isFromMe }?.otpCode ?: ""
                if (code.isNotEmpty()) listOf("Copy code: $code") else emptyList()
            }
            else -> listOf("Sounds good!", "Awesome 👌", "Let me check")
        }
    }

    private fun triggerSimulatedAutoResponse(convId: Int, userMessage: String) {
        viewModelScope.launch {
            val conv = repository.getConversationById(convId) ?: return@launch
            
            // Set typing state to true
            delay(1000)
            _typingStates.value = _typingStates.value.toMutableMap().apply { put(convId, true) }
            
            delay(1500) // typing pause
            _typingStates.value = _typingStates.value.toMutableMap().apply { put(convId, false) }

            val responseBody = when {
                conv.contactPhone == "AI001" -> {
                    // Local AI Assistant response options matching instruction Commands
                    processAICommand(userMessage)
                }
                userMessage.lowercase().contains("otp") || userMessage.lowercase().contains("verification") -> {
                    val randomCode = (100000..999999).random().toString()
                    "ALERT: Your confirmation security pass code is $randomCode. Do not expose this."
                }
                userMessage.lowercase().contains("spam") -> {
                    "FREE CASH! Win $1,000,000 lottery instantly! Submit entry form at link: http://win-now.cc/claim"
                }
                else -> {
                    val replies = listOf(
                        "Got it! That sounds super fun.",
                        "Thanks for the update. Let's touch base later.",
                        "Perfect! See you soon.",
                        "Haha that's amazing! Talk to you in a bit."
                    )
                    replies.random()
                }
            }

            val classified = repository.scanMessageAndClassify(responseBody)
            val incoming = MessageEntity(
                conversationId = convId,
                body = responseBody,
                timestamp = System.currentTimeMillis(),
                isFromMe = false,
                category = classified.category,
                isOtp = classified.isOtp,
                otpCode = classified.otpCode
            )
            repository.insertMessage(incoming)
        }
    }

    private fun processAICommand(msg: String): String {
        val query = msg.lowercase().trim()
        return when {
            query.startsWith("translate") -> {
                val textToTranslate = msg.substringAfter("translate", "").trim()
                if (textToTranslate.isEmpty()) {
                    "To translate, use command syntax: 'translate <text>'. Example: 'translate Hello how are you'"
                } else {
                    val es = translateTextLocally(textToTranslate, "Spanish")
                    val fr = translateTextLocally(textToTranslate, "French")
                    "🌍 **Offline Translation Model**\n- **Spanish (ES):** $es\n- **French (FR):** $fr"
                }
            }
            query.contains("summarize") || query.contains("summary") -> {
                "📝 **Offline Chat Summarizer**\nBased on your local device history:\n- **Sarah Jenkins** is waiting for your reply regarding coffee plans.\n- **Google Security** delivered an OTP passcode (482910).\n- **Netflix Alerts** confirmed a transaction of $15.49."
            }
            query.contains("spam") -> {
                "🛡️ **Local AI Spam Protection**\nOur local active neural filter categorizes messages with suspicious lottery schemes, unsecured short URLs, or cash winnings as SPAM and routes them to your Spam Center automatically."
            }
            else -> {
                "🤖 **Gemini Local Assistant**\nI am configured for offline processing. Commands you can ask me:\n1. 'translate <your text>'\n2. 'summarize' (creates an overview of your index inbox)\n3. 'spam check' (triggers offline classification explanation)\n\nTry sending 'translate Good morning!'"
            }
        }
    }

    private fun translateTextLocally(text: String, lang: String): String {
        return when (lang) {
            "Spanish" -> {
                if (text.contains("hello", true)) "Hola"
                else if (text.contains("good morning", true)) "Buenos días"
                else if (text.contains("coffee", true)) "café"
                else if (text.contains("yes", true)) "Sí"
                else "$text (Traducido)"
            }
            "French" -> {
                if (text.contains("hello", true)) "Bonjour"
                else if (text.contains("good morning", true)) "Bonjour"
                else if (text.contains("coffee", true)) "café"
                else if (text.contains("yes", true)) "Oui"
                else "$text (Traduit)"
            }
            "German" -> "Hallo ($text)"
            else -> text
        }
    }
}
