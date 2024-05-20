package com.example.blog.views.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blog.databinding.ActivityEditBlogBinding
import com.example.blog.models.BlogItemModel
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {

    private val binding: ActivityEditBlogBinding by lazy {
        ActivityEditBlogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val blogItemModel = intent.getSerializableExtra("blogItem") as BlogItemModel

        binding.apply {
            blogTitleEdt.editText?.setText(blogItemModel.heading)
            blogDescriptionEdt.editText?.setText(blogItemModel.post)

            saveBlogBtn.setOnClickListener {
                val updatedTitle = blogTitleEdt.editText?.text.toString().trim()
                val updatedDescription = blogDescriptionEdt.editText?.text.toString().trim()

                if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
                    Toast.makeText(
                        this@EditBlogActivity,
                        "Please fill all the fileds",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    blogItemModel.heading = updatedTitle
                    blogItemModel.post = updatedDescription

                    if (blogItemModel != null) {
                        updateDataInFirebase(blogItemModel)
                    }
                }

            }

            backBtn.setOnClickListener { finish() }
        }

    }

    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("blogs")
        val postId = blogItemModel.postId

        databaseReference.child(postId).setValue(blogItemModel).addOnSuccessListener {
            Toast.makeText(this, "Blog updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong, Please try again", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}