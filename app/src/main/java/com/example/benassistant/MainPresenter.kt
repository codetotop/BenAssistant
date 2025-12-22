package com.example.benassistant

import com.example.benassistant.room.ChatLog
import com.example.benassistant.room.Role
import com.example.benassistant.repository.ChatRepository
import kotlinx.coroutines.*

class MainPresenter(
    private var view: MainContract.View? = null,
    private val repository: ChatRepository
) : MainContract.Presenter {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private var requestJob: Job? = null

    override fun attach(view: MainContract.View) {
        this.view = view
        loadMessages()
    }

    override fun loadMessages() {
        coroutineScope.launch {
            repository.clearExpiredLogs()
            val logs = repository.getChatLogs()
            view?.showMessages(logs)
        }
    }

    override fun removeAll() {
        coroutineScope.launch {
            repository.clearAll()
            view?.showMessages()
        }
    }

    override fun onUserSendMessage(msg: String) {
        view?.addMessage(ChatLog(role = Role.USER, message = msg))
        view?.clearInput()
        view?.showLoading()

        // Cancel previous request
        requestJob?.cancel()

        requestJob = coroutineScope.launch {
            try {
                val chatLog = repository.processUserMessage(msg)
                view?.addMessage(chatLog)
            } catch (e: CancellationException) {
                //cancel hợp lệ (user gửi msg mới / screen destroy)
                return@launch

            } catch (e: Exception) {
                view?.showError("Có lỗi xảy ra")
            } finally {
                // chỉ hide loading nếu job này vẫn là job hiện tại
                if (requestJob == this@launch) {
                    view?.hideLoading()
                }
            }
        }
    }

    override fun detach() {
        view = null
        job.cancel()
    }
}
