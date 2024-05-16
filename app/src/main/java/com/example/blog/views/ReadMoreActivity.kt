package com.example.blog.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.blog.databinding.ActivityReadMoreBinding
import com.example.blog.models.BlogItemModel

class ReadMoreActivity : AppCompatActivity() {

    private val binding: ActivityReadMoreBinding by lazy {
        ActivityReadMoreBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val blogs = intent.getSerializableExtra("blogItem") as BlogItemModel

        binding.apply {

            if (blogs != null) {

                blogs.apply {
                    titleTv.text = heading
                    userNameTv.text = userName
                    dateTv.text = date
                    blogDescriptionTv.text = post

                    Glide.with(this@ReadMoreActivity)
                        .load(profileImage)
                        .into(userProfileImage)

                }
            } else {
                Toast.makeText(this@ReadMoreActivity, "Failed to load data", Toast.LENGTH_SHORT)
                    .show()
            }

            backBtn.setOnClickListener { finish() }

        }

    }
}