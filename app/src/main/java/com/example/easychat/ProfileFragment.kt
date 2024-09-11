package com.example.easychat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.easychat.databinding.FragmentProfileBinding
import com.example.easychat.model.UserModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.messaging.FirebaseMessaging

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri

    private var currentUserModel = UserModel()
    private var inProgressUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data =  result.data
                if (data !=null && data.data !=null) {
                    selectedImageUri = data.data!!
                    AndroidUtil.setProfilePic(
                        requireContext(),
                        selectedImageUri,
                        binding.profilePick
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment using Data Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserData()

        binding.btnUpdateProfile.setOnClickListener{
            if (inProgressUpdate) {
                updateBtnClick()
            }
            setInUpdateProfile(true)
        }

        binding.logoutBtn.setOnClickListener {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener{
                if (it.isSuccessful){
                    FirebaseUtil.logout()
                    val i = Intent(requireContext(),SplashActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                }
            }
        }

        binding.profilePick.setOnClickListener{
            if (inProgressUpdate) {
                ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                    .createIntent {
                        imagePickLauncher.launch(it)
                    }
            }
        }
    }

    private fun updateBtnClick() {
        setInProgress(true)
        val newUsername = binding.profileUsername.text.toString()
        if (newUsername.isEmpty() || newUsername.length < 3){
            binding.profileUsername.error = "Username lengh should be at least 3 chars"
            return
        }
        currentUserModel.username = newUsername
        setInProgress(true)
        if (selectedImageUri!=null) {
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri)
                .addOnCompleteListener{
                    updateToFirestore()
                }
        } else{
            updateToFirestore()
        }
    }

    private fun updateToFirestore(){
        FirebaseUtil.currentUserDetails().set(currentUserModel)
            .addOnCompleteListener{
                setInProgress(false)
                setInUpdateProfile(false)
                if(it.isSuccessful){
                    AndroidUtil.showToast(requireContext(), "Update succesfully")
                } else{
                    AndroidUtil.showToast(requireContext(), "Update failed")
                }
                setInUpdateProfile(false)
            }
    }

    private fun getUserData() {
        setInProgress(true)

        FirebaseUtil.getCurrentProfilePicStorageRef().downloadUrl
            .addOnCompleteListener{
                if(it.isSuccessful){
                    val uri = it.result
                    AndroidUtil.setProfilePic(requireContext(),uri,binding.profilePick)
                }
            }
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener{
            setInProgress(false)
            currentUserModel = it.result.toObject(UserModel::class.java)?: UserModel()
            binding.profilePhone.setText(currentUserModel.phone)
            binding.profileUsername.setText(currentUserModel.username)
        }
    }

    private fun setInProgress(inProgress: Boolean){
        if (inProgress){
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.btnUpdateProfile.visibility = View.GONE
        } else{
            binding.loginProgressBar.visibility = View.GONE
            binding.btnUpdateProfile.visibility = View.VISIBLE
        }
    }

    private fun setInUpdateProfile(inUpdate: Boolean){
        if (inUpdate){
            binding.btnUpdateProfile.text = getString(R.string.txt_save_change)
            binding.profilePhone.isEnabled = true
            binding.profileUsername.isEnabled = true
        } else{
            binding.btnUpdateProfile.text = getString(R.string.txt_update_profile)
            binding.profilePhone.isEnabled = false
            binding.profileUsername.isEnabled = false
        }
        inProgressUpdate = inUpdate
    }
    override fun onPause() {
        super.onPause()
        inProgressUpdate = false
    }
}