package com.example.easychat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.example.easychat.adapter.ChatRecyclerAdapter
import com.example.easychat.adapter.SearchUserRecyclerAdapter
import com.example.easychat.databinding.ActivityChatBinding
import com.example.easychat.model.ChatMessageModel
import com.example.easychat.model.ChatroomModel
import com.example.easychat.model.UserModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.Arrays
import java.util.Date

class ChatActivity : AppCompatActivity() {
    private lateinit var otherUser : UserModel
    private lateinit var chatroomId: String
    private lateinit var adapter: ChatRecyclerAdapter
    private var chatroomModel: ChatroomModel? = null
    private lateinit var binding:ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        otherUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.id)

        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.id).downloadUrl
            .addOnCompleteListener{
                if(it.isSuccessful){
                    val uri = it.result
                    AndroidUtil.setProfilePic(this,uri,binding.profilePick.findViewById(R.id.profile_pic_img_view))
                }
            }
        binding.btnBack.setOnClickListener{
            adapter.stopListening()
            onBackPressed()
        }

        binding.btnSendMessage.setOnClickListener{
            val message = binding.inputChatMessage.text.toString().trim()
            if (message.isEmpty()){
                return@setOnClickListener
            }
            sendMessageToUser(message)
        }

        binding.txtUsername.text = otherUser.username
        getOrCreateChatroomModel()
        setupChatRecyclerView()

    }

    private fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java).build()

        adapter = ChatRecyclerAdapter(options,this)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        binding.chatRecyclerView.layoutManager = linearLayoutManager
        binding.chatRecyclerView.adapter = adapter
        adapter.startListening()
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.chatRecyclerView.smoothScrollToPosition(0)
            }
        }
        adapter.registerAdapterDataObserver(observer)
    }

    private fun sendMessageToUser(message: String) {
        if (chatroomModel != null) {
            chatroomModel!!.lastMessageTimestamp = Timestamp.now()
            chatroomModel!!.lastMessageSenderId = FirebaseUtil.currentUserId()
            chatroomModel!!.lastMessage = message
            FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel!!)

            val chatMessageModel =
                ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now())
            FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding.inputChatMessage.setText("")
                        sendNotification(message)
                    }
                }
        } else{
            getOrCreateChatroomModel()
            sendMessageToUser(message)
        }
    }

    private fun sendNotification(message: String) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener{
            if (it.isSuccessful){
                val currentUser = it.result.toObject(UserModel::class.java)
                try {
                    val jsonObject = JSONObject()
                    val notificationObj = JSONObject()
                    notificationObj.put("title", currentUser!!.username)
                    notificationObj.put("body", message)

                    val dataObj =JSONObject()
                    dataObj.put("id", currentUser.id)

                    jsonObject.put("notification", notificationObj)
                    jsonObject.put("data", dataObj)
                    jsonObject.put("to", otherUser.fcmToken)

                    callApi(jsonObject)
                } catch (e: Exception){
                    Toast.makeText(this, "Algo ha fallado", Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private fun callApi (jsonObject: JSONObject){
        val JSON = "application/json".toMediaType();
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body = jsonObject.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization","Bearer AAAAN4XM4Vw:APA91bGM5K9XOpq3-tCTIYocwzINmOLqACm8CmQEcCAJcEAwagMc-s2lkR5zXwRwnF7VJPJstEdsIhtelHjDGIfScaJevy2WFsjio3dVEDwMGL0j9aY1Vjftpw3BlPugZrcawv3psK8w")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // Se llama cuando la respuesta es exitosa
                val responseBody = response.body?.string() ?: ""
                println("Respuesta de la llamada a la API: $responseBody")
            }

            override fun onFailure(call: Call, e: IOException) {
                // Se llama cuando la llamada falla
                println("Error en la llamada a la API: ${e.message}")
            }
        })
    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener{
            if (it.isSuccessful){
                chatroomModel = it.result.toObject(ChatroomModel::class.java)
                if (chatroomModel == null){
                    val chatroomModel = ChatroomModel(
                        chatroomId,
                        listOf(FirebaseUtil.currentUserId(), otherUser.id) as List<String>,
                        Timestamp.now(),
                        ""
                    )
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel)
                }
            } else{
                Toast.makeText(this, "Ha habido un error al conectarse al chat. Intentelo más tarde.", Toast.LENGTH_LONG).show()
                onBackPressed()
            }
        }
    }
}