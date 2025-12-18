package com.example.kaiaassistant

import com.example.kaiaassistant.data.ChatLog

interface MainContract {

    interface View {

        fun showMessages(messages: List<ChatLog>)
        fun addMessage(message: ChatLog)

        fun showLoading()
        fun hideLoading()

        fun clearInput()

        fun scrollToBottom()
        fun showError(message: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onUserSendMessage(text: String)
        fun loadMessages()
    }
}
