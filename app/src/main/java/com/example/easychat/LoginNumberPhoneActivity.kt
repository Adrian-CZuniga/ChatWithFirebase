package com.example.easychat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.easychat.databinding.ActivityLoginNumberPhoneBinding

class LoginNumberPhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginNumberPhoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginNumberPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginCountryCode.registerCarrierNumberEditText(binding.loginMobilNumber)
        binding.loginProgressBar.visibility = View.GONE

        binding.btnSentOtp.setOnClickListener {
            if (!binding.loginCountryCode.isValidFullNumber){
                binding.loginMobilNumber.error = "Phone number not valid"
                return@setOnClickListener
            }

            val i = Intent(this, LoginOTPActivity::class.java)
            i.putExtra("phone", binding.loginCountryCode.fullNumberWithPlus)
            startActivity(i)

        }
    }
}