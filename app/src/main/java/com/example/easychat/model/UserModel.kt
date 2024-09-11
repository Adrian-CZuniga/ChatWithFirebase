package com.example.easychat.model

import com.google.firebase.Timestamp

class UserModel @JvmOverloads constructor(
    var id: String = "",
    var phone: String = "",
    var username: String = "",
    val createdTimestamp: Timestamp = Timestamp.now(),
    var fcmToken: String = ""
)