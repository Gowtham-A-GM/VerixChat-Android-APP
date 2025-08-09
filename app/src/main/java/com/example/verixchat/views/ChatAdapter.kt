package com.example.verixchat.views

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.verixchat.R
import com.example.verixchat.models.ChatModel
import io.noties.markwon.Markwon

class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val tvChatUser: TextView = itemView.findViewById(R.id.tvChatUser)
    private val tvChatAI: TextView = itemView.findViewById(R.id.tvChatAI)
    private val llChatUser: LinearLayout = itemView.findViewById(R.id.llChatUser)
    private val llChatAI: LinearLayout = itemView.findViewById(R.id.llChatAI)
    private val markwon = Markwon.create(itemView.context)

    fun bind(chatModel: ChatModel) {
        if (chatModel.isUser) {
            llChatUser.visibility = View.VISIBLE
            llChatAI.visibility = View.GONE
            tvChatUser.text = chatModel.message
        } else {
            llChatAI.visibility = View.VISIBLE
            llChatUser.visibility = View.GONE

            // Render AI response with Markdown formatting
            markwon.setMarkdown(tvChatAI, chatModel.message)
        }
    }
}

class ChatAdapter: RecyclerView.Adapter<ChatViewHolder>() {

    private var chats: List<ChatModel> = emptyList()

    fun updateChatList(chats: List<ChatModel>) {
        this.chats = chats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}
