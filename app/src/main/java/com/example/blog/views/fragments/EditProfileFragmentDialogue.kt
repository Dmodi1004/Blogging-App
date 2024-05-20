package com.example.blog.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.blog.R
import com.example.blog.databinding.FragmentEditProfileDialogueBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragmentDialogue : BottomSheetDialogFragment() {

    private val binding: FragmentEditProfileDialogueBinding by lazy {
        FragmentEditProfileDialogueBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    private var profileImageUrl: String? = null
    private var userName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        profileImageUrl = arguments?.getString("profileImageUrl")?.trim()
        userName = arguments?.getString("userName")?.trim()

        loadUserProfileData()

        binding.apply {
            saveBtn.setOnClickListener {
                saveProfileChanges()
                loading.visibility = View.VISIBLE
            }

            materialCardView.setOnClickListener {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    PICK_IMAGE_REQUEST
                )
            }
        }

        return binding.root
    }

    private fun loadUserProfileData() {
        userName?.let {
            binding.userNameEdt.editText?.setText(userName)
        }
        profileImageUrl?.let {
            Glide.with(this@EditProfileFragmentDialogue)
                .load(it)
                .placeholder(R.drawable.ic_user_avatar)
                .into(binding.userprfile)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {

            imageUri = data.data

            Glide.with(this)
                .load(imageUri)
                .into(binding.userprfile)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveProfileChanges() {
        val newUserName = binding.userNameEdt.editText?.text.toString().trim()

        if (newUserName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        if (imageUri != null) {
            val storageReference = storage.reference.child("profileImages/$userId.jpg")
            val uploadTask = storageReference.putFile(imageUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    updateUserProfile(userId, newUserName, downloadUri.toString())
                } else {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            updateUserProfile(userId, newUserName, profileImageUrl)
        }
    }

    private fun updateUserProfile(
        userId: String,
        newUserName: String,
        newProfileImageUrl: String?
    ) {
        val userReference = database.reference.child("users").child(userId)
        val userUpdates = mapOf(
            "name" to newUserName,
            "profileImage" to newProfileImageUrl
        )

        userReference.updateChildren(userUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUserPosts(userId, newUserName, newProfileImageUrl)
                binding.loading.visibility = View.GONE
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT)
                    .show()
                binding.loading.visibility = View.GONE
            }
        }
    }

    private fun updateUserPosts(userId: String, newUserName: String, newProfileImageUrl: String?) {
        val postsReference = database.reference.child("blogs")
        postsReference.orderByChild("userId").equalTo(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                for (postSnapshot in snapshot.children) {
                    val postUpdates = mapOf(
                        "userName" to newUserName,
                        "profileImage" to newProfileImageUrl
                    )
                    postSnapshot.ref.updateChildren(postUpdates)
                }
                Toast.makeText(requireContext(), "Profile update successfully", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Failed to update posts", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}


