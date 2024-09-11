package com.example.easychat

import android.app.DownloadManager.Query
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easychat.adapter.SearchUserRecyclerAdapter
import com.example.easychat.databinding.ActivitySearchUserBinding
import com.example.easychat.model.UserModel
import com.example.easychat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase

class SearchUserActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySearchUserBinding
    private lateinit var adapter:SearchUserRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.inputSearchUsername.requestFocus()

        binding.btnBack.setOnClickListener{
            onBackPressed()
        }

        binding.btnSearch.setOnClickListener{
            val searchTerm = binding.inputSearchUsername.text.toString()
            if (searchTerm.isEmpty() || searchTerm.length<3){
                binding.inputSearchUsername.error = "Invalid user"
                return@setOnClickListener
            }
            setupSearchRecyclerView(searchTerm)
        }
    }

    private fun setupSearchRecyclerView(searchTerm: String) {
        val query = FirebaseUtil.allUserCollectionReference()
            .whereGreaterThanOrEqualTo("username", searchTerm)
            .whereLessThanOrEqualTo("username", searchTerm+'\uf8ff')
        val options = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java).build()

        adapter = SearchUserRecyclerAdapter(options,this)
        binding.searchUserRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchUserRecyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized){
            adapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::adapter.isInitialized){
            adapter.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized){
            adapter.startListening()
        }
    }
}