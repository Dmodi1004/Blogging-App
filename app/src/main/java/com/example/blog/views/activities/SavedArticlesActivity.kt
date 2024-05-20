package com.example.blog.views.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blog.adapters.BlogAdapter
import com.example.blog.databinding.ActivitySavedArticlesBinding
import com.example.blog.models.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedArticlesActivity : AppCompatActivity() {

    private val binding: ActivitySavedArticlesBinding by lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: BlogAdapter
    private val savedBlogArticles = mutableListOf<BlogItemModel>()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference =
                FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(userId).child("savePost")
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as Boolean
                        if (postId != null && isSaved) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)
                                if (blogItem != null) {
                                    savedBlogArticles.add(blogItem)
                                    launch(Dispatchers.Main) {
                                        adapter.updateData(savedBlogArticles)
                                    }
                                    binding.nothingTv.visibility = View.INVISIBLE
                                }
                            }
                        } else {
                            binding.nothingTv.visibility = View.VISIBLE
                        }
                    }
                    binding.loading.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SavedArticlesActivity,
                        "Something went wrong ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.loading.visibility = View.GONE
                }
            })
        }

        setupRecyclerView()

    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("blogs")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            val blogData = dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        } catch (e: Exception) {
            null
        }
    }

    private fun setupRecyclerView() {
        adapter = BlogAdapter(savedBlogArticles.filter { it.isSaved }.toMutableList(), this)
        binding.savedArticlesRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        binding.savedArticlesRecyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()
    }

}