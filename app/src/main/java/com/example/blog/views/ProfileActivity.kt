package com.example.blog.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.blog.R
import com.example.blog.databinding.ActivityProfileBinding
import com.example.blog.views.register.WelcomeActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("users")

        val userId = auth.currentUser?.uid

        if (userId != null) {
            loadUserProfileData(userId)
        }

        binding.addNewBlogBtn.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        binding.logoutBtn.setOnClickListener {

            val view = LayoutInflater.from(this@ProfileActivity)
                .inflate(R.layout.alert_dialog_box, null)

            val titleTv = view.findViewById<TextView>(R.id.titleTv)
            val messageTv = view.findViewById<TextView>(R.id.messageTv)
            val logoutBtn = view.findViewById<MaterialButton>(R.id.positiveBtn)
            val cancelBtn = view.findViewById<MaterialButton>(R.id.negativeBtn)

            val builder = AlertDialog.Builder(this@ProfileActivity)
                .setView(view)

            val dialog = builder.create()

            titleTv.text = "Logout!"
            messageTv.text =
                "Are your sure you want to Logout this account?\nAlways remember your emain and password to Login again."
            logoutBtn.text = "Logout"
            cancelBtn.text = "Cancel"

            logoutBtn.setOnClickListener {
                auth.signOut()
                startActivity(Intent(this, WelcomeActivity::class.java))
                finishAffinity()

                dialog.dismiss()
            }
            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.yourBlogBtn.setOnClickListener {
            startActivity(Intent(this, ArticlesActivity::class.java))
        }

    }

    private fun loadUserProfileData(userId: String) {
        val userReference = databaseReference.child(userId)

        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if (profileImageUrl != null) {
                    Glide.with(this@ProfileActivity)
                        .load(profileImageUrl)
                        .into(binding.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to load user profile image 😢",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        userReference.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.getValue(String::class.java)
                if (userName != null) {
                    binding.userNameTv.text = userName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to load user profile image 😢",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}