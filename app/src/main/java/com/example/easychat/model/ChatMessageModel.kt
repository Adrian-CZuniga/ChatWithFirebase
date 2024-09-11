package com.example.easychat.model

import com.google.firebase.Timestamp


class ChatMessageModel(
    var message:String = "",
    var senderId: String = "",
    var timestamp: Timestamp = Timestamp.now()) {
}