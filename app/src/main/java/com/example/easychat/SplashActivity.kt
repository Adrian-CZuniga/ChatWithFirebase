package com.example.easychat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.easychat.model.UserModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (FirebaseUtil.isLoggedIn() && intent.extras!=null){
            //from notification
            val userId = intent.extras?.getString("id")
            FirebaseUtil.allUserCollectionReference().document(userId!!).get()
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        val model = it.result.toObject(UserModel::class.java)
                        val mainIntent = Intent(this, MainActivity::class.java)
                        mainIntent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                        startActivity(mainIntent)
                        var i = Intent()
                        if (model != null) {
                            i = Intent(this, ChatActivity::class.java)
                            AndroidUtil.passUserModelAsIntent(i, model)
                        } else{
                            Toast.makeText(this, "¡Error inesperado!", Toast.LENGTH_SHORT).show()
                        }
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(i)
                        finish()
                    }
                }
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                if (FirebaseUtil.isLoggedIn()) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginNumberPhoneActivity::class.java))
                }
                finish()
            }, 1500)
        }

    }
}