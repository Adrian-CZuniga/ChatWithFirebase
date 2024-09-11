package com.example.easychat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.easychat.databinding.ActivityLoginOtpactivityBinding
import com.example.easychat.utils.AndroidUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class LoginOTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginOtpactivityBinding
    private val mAuth = FirebaseAuth.getInstance()
    private var timeoutSeconds = 30L
    private lateinit var verificationCode: String
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String

    private val TAG = LoginOTPActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.extras?.getString("phone", "").toString()
        phoneNumber.let { sendOTP(it, false) }

        binding.btnLoginOtp.setOnClickListener {
            val enteredOTP = binding.loginOtp.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationCode, enteredOTP)
            signIn(credential)
            setInProgress(true)
        }

        binding.txtviewResendOtp.setOnClickListener{
            sendOTP(phoneNumber, true)
        }
    }

    private fun sendOTP(phoneNumber: String, isResend: Boolean ){
        startResendTimer()
        setInProgress(true)
        val builder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signIn(credential)
                    setInProgress(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.d(TAG, "OTP verification fail:  $e")
                    AndroidUtil.showToast(applicationContext, "OTP verification fail:  $e")
                    setInProgress(false)
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verificationId, token)
                    verificationCode = verificationId
                    resendingToken = token
                    Log.d(TAG, "onCodeSent:$verificationId")
                    Log.d(TAG, "onTokenSent:$token")
                    AndroidUtil.showToast(applicationContext, "OTP sent successfully")
                    setInProgress(false)
                }
            })

        if (isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build())
        }else{
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    private fun startResendTimer() {
        binding.txtviewResendOtp.isEnabled = false
        val timer = Timer()
        timer.scheduleAtFixedRate(timerTask {
            timeoutSeconds--
            binding.txtviewResendOtp.text = "Resend OTP in $timeoutSeconds seconds"
            if (timeoutSeconds <=0){
                timeoutSeconds = 30L
                timer.cancel()
                runOnUiThread(Runnable {
                    binding.txtviewResendOtp.isEnabled = true
                })

            }
        }, 0, 1000)
    }

    private fun signIn(credential: PhoneAuthCredential) {
        setInProgress(true)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val i = Intent(this, LoginUsernameActivity::class.java)
                    i.putExtra("phoneNumber", phoneNumber)
                    startActivity(i)
                } else {
                    AndroidUtil.showToast(this, "Otp verification failed")
                    setInProgress(false)
                }
            }
    }

    fun setInProgress(inProgress: Boolean){
        if (inProgress){
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.btnLoginOtp.visibility = View.GONE
        } else{
            binding.loginProgressBar.visibility = View.GONE
            binding.btnLoginOtp.visibility = View.VISIBLE
        }
    }
}