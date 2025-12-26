package com.example.benassistant

import com.example.benassistant.room.ChatLog

interface MainContract {

    interface View {

        fun loadMessages(messages: List<ChatLog>? = listOf())
        fun addMessage(message: ChatLog)
        fun setMessage(message: ChatLog)
        fun removeLastMessage()

        fun beginTransactionUI()
        fun closeTransactionUI()

        fun clearInput()

        fun scrollToBottom()
        fun showError(message: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onUserSendMessage(text: String)
        fun loadMessages()
        fun removeAll()
        fun cancelRequest()
    }
}
