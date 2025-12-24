package com.example.benassistant.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_log")
data class ChatLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val role: Role, // user | assistant
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),

    var isNew: Boolean = false // Track if the message is new
)

enum class Role {
    USER,
    ASSISTANT
}
