package com.example.easychat.utils

import com.google.apphosting.datastore.testing.DatastoreTestTrace.FirestoreV1Action.ListCollectionIds
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat

class FirebaseUtil {
    companion object {
        fun isLoggedIn(): Boolean{
            if (currentUserId() != "null"){
                return true
            }
            return false
        }
         fun currentUserId(): String {
            return FirebaseAuth.getInstance().uid.toString()
        }
        fun currentUserDetails(): DocumentReference {
            return FirebaseFirestore.getInstance().collection("users").document(currentUserId())
        }

        fun allUserCollectionReference(): CollectionReference{
            return FirebaseFirestore.getInstance().collection("users")
        }

        fun getChatroomReference(chatroomId: String):DocumentReference{
            return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)
        }

        fun getChatroomMessageReference(chatroomId: String): CollectionReference{
            return getChatroomReference(chatroomId).collection("chats")
        }

        fun allChatroomCollectionReference(): CollectionReference{
            return FirebaseFirestore.getInstance().collection("chatrooms")
        }

        fun getOtherUserFromChatroom(userIds: List<String>): DocumentReference{
            return if(userIds.get(0).equals(currentUserId())){
                allUserCollectionReference().document(userIds.get(1))
            } else{
                allUserCollectionReference().document(userIds.get(0))
            }
        }

        fun getChatroomId(userId1:String, userId2:String):String {
            return if (userId1.hashCode() < userId2.hashCode()) {
                userId1 + "_" + userId2
            } else {
                userId2 + "_" + userId1
            }
        }

        fun timestampToString(timestamp: Timestamp) : String{
            return SimpleDateFormat("HH:MM").format(timestamp.toDate())
        }

        fun getCurrentProfilePicStorageRef(): StorageReference {
            return FirebaseStorage.getInstance().reference.child("profile_pic")
                .child(FirebaseUtil.currentUserId())
        }

        fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
            return FirebaseStorage.getInstance().reference.child("profile_pic")
                .child(otherUserId)
        }

        fun logout(){
            FirebaseAuth.getInstance().signOut()
        }
    }
}