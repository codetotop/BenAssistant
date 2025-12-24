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
            view?.loadMessages(logs)
        }
    }

    override fun removeAll() {
        coroutineScope.launch {
            repository.clearAll()
            view?.loadMessages()
        }
    }

    override fun onUserSendMessage(msg: String) {
        view?.addMessage(ChatLog(role = Role.USER, message = msg))
        view?.addMessage(ChatLog(role = Role.ASSISTANT, message = null, isNew = true))
        view?.clearInput()
        view?.beginTransactionUI()

        // Cancel previous request
        requestJob?.cancel()

        requestJob = coroutineScope.launch {
            try {
                val chatLog = repository.processUserMessage(msg)
                view?.setMessage(chatLog)
            } catch (e: CancellationException) {
                //cancel hợp lệ (user gửi msg mới / screen destroy)
                return@launch

            } catch (e: Exception) {
                view?.showError("Có lỗi xảy ra")
            } finally {
                // chỉ hide loading nếu job này vẫn là job hiện tại
                if (requestJob == this@launch) {
                    view?.closeTransactionUI()
                }
            }
        }
    }

    override fun cancelRequest() {
        requestJob?.cancel()
        view?.closeTransactionUI()

        // Set cancel message directly to the last assistant message
        val cancelMessage = ChatLog(
            role = Role.ASSISTANT,
            message = "✋ Ben đã hủy yêu cầu vừa rồi",
            isNew = false
        )
        view?.setMessage(cancelMessage)
    }

    override fun detach() {
        view = null
        job.cancel()
    }
}
