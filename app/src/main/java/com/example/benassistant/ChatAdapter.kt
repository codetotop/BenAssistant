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
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatLog>?) {
        messages.clear()
        messages.addAll(newMessages?: emptyList())
        notifyDataSetChanged()
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    class UserVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(log: ChatLog) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = log.message
        }
    }

    class AssistantVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(log: ChatLog) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = log.message
        }
    }
}
