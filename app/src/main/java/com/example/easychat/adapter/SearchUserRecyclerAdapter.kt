package com.example.easychat.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.easychat.ChatActivity
import com.example.easychat.R
import com.example.easychat.databinding.LayoutUserItemRecyclerViewBinding
import com.example.easychat.model.UserModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot

private val TAG = SearchUserRecyclerAdapter::class.java.simpleName
class SearchUserRecyclerAdapter(options: FirestoreRecyclerOptions<UserModel>, val context: Context) :
    FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserModelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutUserItemRecyclerViewBinding.inflate(inflater, parent, false)
        return UserModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserModelViewHolder, position: Int, model: UserModel) {
        holder.bind(model)
        holder.itemView.setOnClickListener{
            val i = Intent(context, ChatActivity::class.java)
            AndroidUtil.passUserModelAsIntent(i, model)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }
    }

    inner class UserModelViewHolder(private val binding: LayoutUserItemRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userModel: UserModel) {
            binding.phoneText.text = userModel.phone
            binding.userNameText.text = userModel.username
            if (userModel.id == FirebaseUtil.currentUserId()){
                binding.userNameText.text = "${userModel.username} (Me)"
            }

            FirebaseUtil.getOtherProfilePicStorageRef(userModel.id).downloadUrl
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        val uri = it.result
                        AndroidUtil.setProfilePic(context,uri,binding.profilePick.findViewById(R.id.profile_pic_img_view))
                    }
                }
            binding.executePendingBindings()
        }
    }
}