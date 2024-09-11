package com.example.easychat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.easychat.databinding.ActivityMainBinding
import com.example.easychat.utils.FirebaseUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatFragment: ChatFragment
    private lateinit var profileFragment: ProfileFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatFragment = ChatFragment()
        profileFragment = ProfileFragment()

        binding.btnMainSearch.setOnClickListener{
            startActivity(Intent(this, SearchUserActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if(item.itemId == R.id.menu_chat) {
                supportFragmentManager.beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit()
            }
            if (item.itemId==R.id.menu_profile){
                supportFragmentManager.beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit()
            }
            return@setOnItemSelectedListener true
        }
        binding.bottomNavigation.selectedItemId = R.id.menu_chat

        getFCMToken()

    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            if (it.isSuccessful){
                val token = it.result
                FirebaseUtil.currentUserDetails().update("fcmToken", token)
            }
        }
    }
}