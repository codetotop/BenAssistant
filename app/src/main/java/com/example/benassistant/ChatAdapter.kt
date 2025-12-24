package com.example.benassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.benassistant.room.ChatLog
import com.example.benassistant.room.Role

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val USER = 0
        private const val ASSISTANT = 1
    }

    private var messages = mutableListOf<ChatLog>()

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == Role.USER) USER else ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == USER) {
            UserVH(inflater.inflate(R.layout.item_user_message, parent, false))
        } else {
            AssistantVH(inflater.inflate(R.layout.item_assistant_message, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val log = messages[position]
        when (holder) {
            is UserVH -> holder.bind(log)
            is AssistantVH -> holder.bind(log)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatLog) {
        message.isNew = true
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatLog>?) {
        messages.clear()
        messages.addAll(newMessages?.onEach { it.isNew = false } ?: emptyList())
        notifyDataSetChanged()
    }

    class UserVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(log: ChatLog) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = log.message
        }
    }

    class AssistantVH(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = itemView.findViewById(R.id.tvMessage)
        private var currentRunnable: Runnable? = null

        fun bind(log: ChatLog) {
            textView.text = "" // Clear previous text
            val message = log.message
            if (log.isNew) {
                displayTextGradually(message)
                log.isNew = false
            } else {
                textView.text = message
            }
        }

        private fun displayTextGradually(message: String) {
            val delayMillis: Long = 30

            // Cancel previous Runnable if exists
            currentRunnable?.let { itemView.removeCallbacks(it) }

            val runnable = object : Runnable {
                var currentIndex = 0

                override fun run() {
                    if (currentIndex < message.length) {
                        textView.append(message[currentIndex].toString())
                        currentIndex++
                        itemView.postDelayed(this, delayMillis)
                    }


                }
            }
            currentRunnable = runnable
            itemView.post(runnable)
        }
    }
}
