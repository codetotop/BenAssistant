package com.example.benassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
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

    fun setMessage(message: ChatLog) {
        val indexLast = messages.size - 1
        messages[indexLast] = message
        notifyItemChanged(indexLast)
    }

    fun loadMessages(newMessages: List<ChatLog>?) {
        val oldSize = messages.size
        messages.clear()
        notifyItemRangeRemoved(0, oldSize)

        newMessages?.let {
            messages.addAll(it.onEach { log -> log.isNew = false })
            notifyItemRangeInserted(0, it.size)
        }
    }

    class UserVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(log: ChatLog) {
            log.message?.let { message ->
                itemView.findViewById<TextView>(R.id.tvMessage).text = message
            }
        }
    }

    class AssistantVH(view: View) : RecyclerView.ViewHolder(view) {
        private val typingIndicator: LinearLayout = itemView.findViewById(R.id.typingIndicator)
        private val dot1: TextView = itemView.findViewById(R.id.dot1)
        private val dot2: TextView = itemView.findViewById(R.id.dot2)
        private val dot3: TextView = itemView.findViewById(R.id.dot3)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private var currentRunnable: Runnable? = null

        fun bind(log: ChatLog) {
            tvMessage.text = "" // Clear previous text

            if (log.message == null && log.isNew) {
                typingIndicator.visibility = View.VISIBLE
                startTypingAnimation()
            } else {
                typingIndicator.visibility = View.GONE
                stopTypingAnimation()
            }

            log.message?.let { message ->
                if (log.isNew) {
                    displayTextGradually(message)
                    log.isNew = false
                } else {
                    tvMessage.text = message
                }
            }
        }

        private fun startTypingAnimation() {
            val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.typing_dot)

            // Start animations with delays to create wave effect
            dot1.startAnimation(animation)
            dot2.postDelayed({ dot2.startAnimation(animation) }, 200)
            dot3.postDelayed({ dot3.startAnimation(animation) }, 400)
        }

        private fun stopTypingAnimation() {
            dot1.clearAnimation()
            dot2.clearAnimation()
            dot3.clearAnimation()
        }

        private fun displayTextGradually(message: String) {
            val delayMillis: Long = 30

            // Cancel previous Runnable if exists
            currentRunnable?.let { itemView.removeCallbacks(it) }

            val runnable = object : Runnable {
                var currentIndex = 0

                override fun run() {
                    if (currentIndex < message.length) {
                        tvMessage.append(message[currentIndex].toString())
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
