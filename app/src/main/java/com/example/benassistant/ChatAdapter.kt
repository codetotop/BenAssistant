package com.example.benassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.benassistant.room.ChatLog
import com.example.benassistant.room.Role

class ChatAdapter : ListAdapter<ChatLog, RecyclerView.ViewHolder>(Diff) {

    companion object {
        private const val USER = 0
        private const val ASSISTANT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == Role.USER) USER else ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == USER) {
            UserVH(inflater.inflate(R.layout.item_user_message, parent, false))
        } else {
            AssistantVH(inflater.inflate(R.layout.item_assistant_message, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val log = getItem(position)
        when (holder) {
            is UserVH -> holder.bind(log)
            is AssistantVH -> holder.bind(log)
        }
    }

    fun addMessage(message: ChatLog) {
        val newList = currentList.toMutableList()
        newList.add(message)
        submitList(newList)
    }

    object Diff : DiffUtil.ItemCallback<ChatLog>() {
        override fun areItemsTheSame(old: ChatLog, new: ChatLog) = old.id == new.id
        override fun areContentsTheSame(old: ChatLog, new: ChatLog) = old == new
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
