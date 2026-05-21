package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversationId: Int,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean,
    val status: String = "SENT", // PENDING, SENT, DELIVERED, READ
    val isScheduled: Boolean = false,
    val scheduledTime: Long? = null,
    val category: String = "PERSONAL", // PERSONAL, TRANSACTION, OTP, SPAM
    val isOtp: Boolean = false,
    val otpCode: String? = null,
    val attachmentPath: String? = null, // Mock local description or custom attachment path
    val translatedText: String? = null
)
