package com.example.easychat.model

import com.google.firebase.Timestamp

class ChatroomModel(
    var chatRoomId: String = "",
    var userIds: List<String> = emptyList(),
    var lastMessageTimestamp: Timestamp = Timestamp.now(),
    var lastMessageSenderId: String = "",
    var lastMessage: String = ""
)