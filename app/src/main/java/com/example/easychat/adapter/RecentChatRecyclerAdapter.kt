package com.example.easychat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easychat.ChatActivity
import com.example.easychat.R
import com.example.easychat.model.ChatroomModel
import com.example.easychat.model.UserModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

private val TAG = RecentChatRecyclerAdapter::class.java.simpleName
class RecentChatRecyclerAdapter (options: FirestoreRecyclerOptions<ChatroomModel>, val context: Context) :
    FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.recent_chat_item_recycler, parent, false)
        return ChatroomModelViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatroomModelViewHolder, position: Int, model: ChatroomModel) {
        holder.bind(model)
    }

    inner class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var usernameText : TextView
        private lateinit var lastMessageText: TextView
        private lateinit var  lastMessageTime: TextView
        private lateinit var profilePic: ImageView
        fun bind(chatroomModel: ChatroomModel) {
            usernameText = itemView.findViewById(R.id.txt_user_name)
            lastMessageText = itemView.findViewById(R.id.txt_last_message)
            lastMessageTime = itemView.findViewById(R.id.txt_last_message_time)
            profilePic = itemView.findViewById(R.id.profile_pic_img_view)

            FirebaseUtil.getOtherUserFromChatroom(chatroomModel.userIds)
                .get().addOnCompleteListener {
                    if (it.isSuccessful){
                        val lastMessageSentByMe = chatroomModel.lastMessageSenderId == FirebaseUtil.currentUserId()
                        val otherUserModel = it.result.toObject(UserModel::class.java)

                        otherUserModel?.let { it1 ->
                            FirebaseUtil.getOtherProfilePicStorageRef(it1.id).downloadUrl
                                .addOnCompleteListener{
                                    if(it.isSuccessful){
                                        val uri = it.result
                                        AndroidUtil.setProfilePic(context,uri,profilePic)
                                    }
                                }
                        }

                        usernameText.text = otherUserModel?.username.toString()
                        if (lastMessageSentByMe) {
                            lastMessageText.text = "You: ${chatroomModel.lastMessage}"
                        } else{
                            lastMessageText.text = chatroomModel.lastMessage
                        }

                        lastMessageTime.text = FirebaseUtil.timestampToString(chatroomModel.lastMessageTimestamp)

                        itemView.setOnClickListener{
                            val i = Intent(context, ChatActivity::class.java)
                            if (otherUserModel != null) {
                                AndroidUtil.passUserModelAsIntent(i, otherUserModel)
                            }
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(i)
                        }
                    }
                }
        }
    }
}