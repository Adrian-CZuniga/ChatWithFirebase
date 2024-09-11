package com.example.easychat.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.easychat.model.UserModel

class AndroidUtil {
    companion object{
        fun showToast(context: Context, message: String){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun passUserModelAsIntent(i: Intent, userModel: UserModel){
            i.putExtra("username", userModel.username)
            i.putExtra("phone", userModel.phone)
            i.putExtra("userId", userModel.id)
            i.putExtra("fcmToken", userModel.fcmToken)
        }

        fun getUserModelFromIntent(i:Intent): UserModel{
            val userModel= UserModel()
            userModel.username = i.getStringExtra("username").toString()
            userModel.phone = i.getStringExtra("phone").toString()
            userModel.id = i.getStringExtra("userId").toString()
            userModel.fcmToken = i.getStringExtra("fcmToken").toString()
            return userModel
        }

        fun setProfilePic(context: Context, imageUri: Uri, imageView: ImageView){
            Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView)
        }
    }
}