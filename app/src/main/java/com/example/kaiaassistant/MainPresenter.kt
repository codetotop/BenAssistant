package com.example.kaiaassistant

import com.example.kaiaassistant.data.ChatLog
import com.example.kaiaassistant.data.Role
import com.example.kaiaassistant.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainPresenter(
    private var view: MainContract.View? = null,
    private val repository: ChatRepository,
    private val coroutineScope: CoroutineScope = MainScope()
) : MainContract.Presenter {

    override fun attach(view: MainContract.View) {
        this.view = view
        loadTodayMessages()
    }

    override fun loadTodayMessages() {
        coroutineScope.launch {
            val logs = repository.getTodayLogs()
            view?.showMessages(logs)
            view?.scrollToBottom()
        }
    }

    override fun loadAllMessages() {
        coroutineScope.launch {
            val logs = repository.getChatLogs()
            view?.showMessages(logs)
            view?.scrollToBottom()
        }
    }

    override fun onUserSendMessage(msg: String) {
        view?.addMessage(ChatLog(role = Role.USER, message = msg))
        view?.clearInput()
        view?.showLoading()

        coroutineScope.launch {
            try {
                repository.processUserMessage(msg)
                val logs = repository.getTodayLogs()
                view?.showMessages(logs)
                view?.scrollToBottom()
            } catch (e: Exception) {
                view?.showError("Có lỗi xảy ra")
            } finally {
                view?.hideLoading()
            }
        }
    }

    override fun detach() {
        view = null
        coroutineScope.cancel()
    }
}
