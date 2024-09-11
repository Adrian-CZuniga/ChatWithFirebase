package com.example.easychat.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.easychat.ChatActivity
import com.example.easychat.databinding.ChatMessageItemRecyclerBinding
import com.example.easychat.databinding.LayoutUserItemRecyclerViewBinding
import com.example.easychat.model.ChatMessageModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot

private val TAG = ChatRecyclerAdapter::class.java.simpleName
class ChatRecyclerAdapter(options: FirestoreRecyclerOptions<ChatMessageModel>, val context: Context) :
    FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatMessageItemRecyclerBinding.inflate(inflater, parent, false)
        return ChatModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatModelViewHolder, position: Int, model: ChatMessageModel) {
        holder.bind(model)
    }

    inner class ChatModelViewHolder(private val binding: ChatMessageItemRecyclerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ChatMessageModel: ChatMessageModel) {
            if (ChatMessageModel.senderId.equals(FirebaseUtil.currentUserId())){
                binding.leftChatLayout.visibility = View.GONE
                binding.rightChatLayout.visibility = View.VISIBLE
                binding.rightChatTextview.text = ChatMessageModel.message
            } else{
                binding.leftChatLayout.visibility = View.VISIBLE
                binding.rightChatLayout.visibility = View.GONE
                binding.leftChatTextview.text = ChatMessageModel.message
            }
        }
    }
}