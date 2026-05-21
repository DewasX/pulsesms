package com.example.data.repository

import com.example.data.local.ConversationEntity
import com.example.data.local.MessageDao
import com.example.data.local.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MessageRepository(private val messageDao: MessageDao) {

    val activeConversations: Flow<List<ConversationEntity>> = messageDao.getActiveConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = messageDao.getArchivedConversations()
    val spamConversations: Flow<List<ConversationEntity>> = messageDao.getSpamConversations()

    fun getMessages(conversationId: Int): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    fun getScheduledMessages(): Flow<List<MessageEntity>> =
        messageDao.getScheduledMessages()

    fun searchMessages(query: String): Flow<List<MessageEntity>> =
        messageDao.searchMessages(query)

    suspend fun getConversationById(id: Int): ConversationEntity? =
        messageDao.getConversationById(id)

    suspend fun insertConversation(conversation: ConversationEntity): Long =
        messageDao.insertConversation(conversation)

    suspend fun updateConversation(conversation: ConversationEntity) =
        messageDao.updateConversation(conversation)

    suspend fun deleteConversation(id: Int) =
        messageDao.deleteConversation(id)

    suspend fun getConversationByPhone(phone: String): ConversationEntity? =
        messageDao.getConversationByPhone(phone)

    suspend fun insertMessage(message: MessageEntity): Long {
        val messageId = messageDao.insertMessage(message)
        // Update the corresponding conversation with last message details
        val conversation = messageDao.getConversationById(message.conversationId)
        if (conversation != null) {
            val updated = conversation.copy(
                lastMessage = message.body,
                lastMessageTimestamp = message.timestamp,
                unreadCount = if (message.isFromMe) 0 else conversation.unreadCount + 1
            )
            messageDao.updateConversation(updated)
        }
        return messageId
    }

    suspend fun updateMessage(message: MessageEntity) =
        messageDao.updateMessage(message)

    suspend fun deleteMessage(id: Int) =
        messageDao.deleteMessage(id)

    // Dynamic offline scanning & categorization for incoming messages
    fun scanMessageAndClassify(body: String): ClassifiedProperties {
        val upper = body.uppercase()
        
        // Spam protection check offline
        if (upper.contains("WIN") || upper.contains("CONGRATULATIONS") || upper.contains("CASH PRIZE") ||
            upper.contains("LOTTERY") || upper.contains("CLAIM NOW") || upper.contains("FREE-GIFT") ||
            upper.contains("CASINO") || upper.contains("http://") || upper.contains("https://") && (upper.contains("CASINO") || upper.contains("FREE"))
        ) {
            return ClassifiedProperties(category = "SPAM", isOtp = false, otpCode = null)
        }

        // OTP detection check offline
        val digitsPattern = """\b\d{4,8}\b""".toRegex()
        val matchResult = digitsPattern.find(body)
        if (matchResult != null && (upper.contains("CODE") || upper.contains("OTP") || upper.contains("VERIFY") || upper.contains("VERIFICATION") || upper.contains("SECURITY"))) {
            return ClassifiedProperties(category = "OTP", isOtp = true, otpCode = matchResult.value)
        }

        // Transaction check offline
        if (upper.contains("PAYMENT") || upper.contains("BILL") || upper.contains("SUBSCRIBE") ||
            upper.contains("RENEW") || upper.contains("SHIPPED") || upper.contains("DELIVERY") ||
            upper.contains("DEBITED") || upper.contains("CREDITED") || upper.contains("RECEIPT") ||
            upper.contains("ORDER ID") || upper.contains("TRANSACTION")
        ) {
            return ClassifiedProperties(category = "TRANSACTION", isOtp = false, otpCode = null)
        }

        return ClassifiedProperties(category = "PERSONAL", isOtp = false, otpCode = null)
    }

    suspend fun populateMockData() = withContext(Dispatchers.IO) {
        val existing = activeConversations.first()
        val archived = archivedConversations.first()
        val spam = spamConversations.first()
        if (existing.isNotEmpty() || archived.isNotEmpty() || spam.isNotEmpty()) return@withContext // Already populated

        // Create initial conversations
        // ID 1: local AI assistant
        val assistantsId = messageDao.insertConversation(
            ConversationEntity(
                id = 1,
                contactName = "Google AI Assistant",
                contactPhone = "AI001",
                avatarColorHex = "#1A73E8",
                isPinned = true
            )
        ).toInt()

        // ID 2: Netflix alerts
        val netflixId = messageDao.insertConversation(
            ConversationEntity(
                id = 2,
                contactName = "Netflix Security & Alerts",
                contactPhone = "NFLX-STAL",
                avatarColorHex = "#E50914"
            )
        ).toInt()

        // ID 3: Google account code
        val googleId = messageDao.insertConversation(
            ConversationEntity(
                id = 3,
                contactName = "Google",
                contactPhone = "22000",
                avatarColorHex = "#4285F4"
            )
        ).toInt()

        // ID 4: Sarah
        val sarahId = messageDao.insertConversation(
            ConversationEntity(
                id = 4,
                contactName = "Sarah Jenkins",
                contactPhone = "+1 (555) 382-9021",
                avatarColorHex = "#34A853"
            )
        ).toInt()

        // ID 5: Spam Promo
        val spamerId = messageDao.insertConversation(
            ConversationEntity(
                id = 5,
                contactName = "Lucky Sweepstakes",
                contactPhone = "Promo-Spam",
                avatarColorHex = "#EA4335",
                isSpam = true
            )
        ).toInt()

        // Insert messages for each conversation
        // Assistant
        insertMessage(MessageEntity(
            conversationId = assistantsId,
            body = "Hi! I am your personal local Gemini Messaging Assistant. Type a command inside this thread like 'translate <text>' or 'summarize' or ask a general question!\nTry sending: 'Hello there assistant!'",
            isFromMe = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 12,
            category = "PERSONAL"
        ))
        
        // Netflix alerts
        insertMessage(MessageEntity(
            conversationId = netflixId,
            body = "Your subscription renewed today! Thank you for your payment of $15.49. Order ID: NFLX-90281-PAY.",
            isFromMe = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 8,
            category = "TRANSACTION"
        ))

        // Google OTP
        insertMessage(MessageEntity(
            conversationId = googleId,
            body = "Your Google account verification code is 482910. Do not share it with anyone.",
            isFromMe = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            category = "OTP",
            isOtp = true,
            otpCode = "482910"
        ))

        // Sarah
        insertMessage(MessageEntity(
            conversationId = sarahId,
            body = "Hey! Let me know if you are still up for coffee later at 4 PM.",
            isFromMe = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 1,
            category = "PERSONAL"
        ))

        // Spam
        insertMessage(MessageEntity(
            conversationId = spamerId,
            body = "CLAIM NOW! You have won a $1000 Amazon gift card! Click here http://win-amazon-claim.lucky to redeem today.",
            isFromMe = false,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            category = "SPAM"
        ))
    }
}

data class ClassifiedProperties(
    val category: String,
    val isOtp: Boolean,
    val otpCode: String?
)
