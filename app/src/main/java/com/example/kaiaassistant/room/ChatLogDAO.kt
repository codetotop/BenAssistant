package com.example.kaiaassistant.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatLogDao {

    @Query("SELECT * FROM chat_log WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp ASC")
    suspend fun getTodayLogs(startOfDay: Long, endOfDay: Long): List<ChatLog>

    @Query("SELECT * FROM chat_log ORDER BY timestamp ASC")
    suspend fun getLogs(): List<ChatLog>

    @Insert
    suspend fun insert(log: ChatLog)

    @Query("DELETE FROM chat_log WHERE timestamp < :expiredTime")
    suspend fun deleteOlderThan(expiredTime: Long)
}


