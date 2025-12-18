package com.example.kaiaassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatLogDao {

    @Query("SELECT * FROM chat_log ORDER BY timestamp ASC")
    suspend fun getLogs(): List<ChatLog>

    @Insert
    suspend fun insert(log: ChatLog)

    @Query("DELETE FROM chat_log WHERE timestamp < :expiredTime")
    suspend fun deleteOlderThan(expiredTime: Long)
}


