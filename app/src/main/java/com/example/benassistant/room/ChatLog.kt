package com.example.benassistant.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_log")
data class ChatLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val role: Role, // user | assistant
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis(),

    var isNew: Boolean = false, // Track if the message is new

    @Embedded
    val alarm: Alarm? = null, // Embedded object for alarm details

    val destination: String? = null
)

data class Alarm(
    val label: String,
    val hour: Int,
    val minute: Int
)

enum class Role {
    USER,
    ASSISTANT
}
