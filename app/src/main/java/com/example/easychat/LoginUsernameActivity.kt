package com.example.easychat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.easychat.databinding.ActivityLoginUsernameBinding
import com.example.easychat.model.UserModel
import com.example.easychat.utils.FirebaseUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp

class LoginUsernameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginUsernameBinding
    private lateinit var phoneNumber: String
    private var userModel: UserModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.extras?.getString("phoneNumber", "") ?: ""
        getUsername()

        binding.btnLoginAccount.setOnClickListener{
            setUsername()
        }

    }

    private fun setUsername(){
        setInProgress(true)
        val username = binding.loginUsername.text.toString()
        if (username.isEmpty() || username.length < 3){
            binding.loginUsername.error = "Username lengh should be at least 3 chars"
            return
        }
        if (userModel != null){
            userModel!!.username =username
        } else{
            userModel = UserModel(FirebaseUtil.currentUserId(),phoneNumber, username, Timestamp.now())
        }

        FirebaseUtil.currentUserDetails().set(userModel!!).addOnCompleteListener{
            setInProgress(false)
            if (it.isSuccessful){
                val i = Intent(this, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }
        }
    }

    private fun getUsername() {
        setInProgress(true)
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(OnCompleteListener {
            setInProgress(false)
            if (it.isSuccessful){
                userModel = it.result.toObject(UserModel::class.java)

                if (userModel !=null) binding.loginUsername.setText(userModel!!.username)
            }
        })
    }

    private fun setInProgress(inProgress: Boolean){
        if (inProgress){
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.btnLoginAccount.visibility = View.GONE
        } else{
            binding.loginProgressBar.visibility = View.GONE
            binding.btnLoginAccount.visibility = View.VISIBLE
        }
    }
}