package com.example.blog.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blog.databinding.ActivityAddArticleBinding
import com.example.blog.models.BlogItemModel
import com.example.blog.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {

    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {

            backBtn.setOnClickListener { finish() }

            addBlogBtn.setOnClickListener {

                val title = blogTitleEdt.editText?.text.toString().trim()
                val description = blogDescriptionEdt.editText?.text.toString().trim()

                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(
                        this@AddArticleActivity,
                        "Please fill all the fields",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val user: FirebaseUser? = auth.currentUser

                if (user != null) {
                    val userId = user.uid
                    val userName = user.displayName ?: "Anonymous"
                    val userImageUrl = user.photoUrl ?: ""

                    userReference.child(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userData = snapshot.getValue(UserData::class.java)

                                if (userData != null) {
                                    val userNameFromDB = userData.name
                                    val userImageUrlFromDB = userData.profileImage

                                    val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                                    val blogItem = BlogItemModel(
                                        heading = title,
                                        userName = userNameFromDB,
                                        date = currentDate,
                                        post = description,
                                        likeCount = 0,
                                        profileImage = userImageUrlFromDB,
                                        userId = userId
                                    )

                                    val key = databaseReference.push().key
                                    if (key != null) {
                                        blogItem.postId = key
                                        val blogReference = databaseReference.child(key)
                                        blogReference.setValue(blogItem).addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this@AddArticleActivity,
                                                    "Failed to add blog",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@AddArticleActivity,
                                    error.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        })
                }

            }

        }

    }
}