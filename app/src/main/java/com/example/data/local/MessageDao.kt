package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    // Conversations
    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND isSpam = 0 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY lastMessageTimestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isSpam = 1 ORDER BY lastMessageTimestamp DESC")
    fun getSpamConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Int): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE contactPhone = :phone LIMIT 1")
    suspend fun getConversationByPhone(phone: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Int)

    // Messages
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isScheduled = 1 ORDER BY timestamp ASC")
    fun getScheduledMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isScheduled = 0 ORDER BY timestamp DESC")
    fun getAllDeliveredMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Int): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Int)

    // Search query matches conversation contents
    @Query("SELECT * FROM messages WHERE body LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<MessageEntity>>
}
