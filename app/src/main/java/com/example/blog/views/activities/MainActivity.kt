package com.example.blog.views.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blog.R
import com.example.blog.adapters.BlogAdapter
import com.example.blog.databinding.ActivityMainBinding
import com.example.blog.models.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: BlogAdapter

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val blogItems = mutableListOf<BlogItemModel>()
    private val allBlogItems = mutableListOf<BlogItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "blogs"
            )

        val userId = auth.currentUser?.uid

        if (userId != null) {
            loadUserProfileImage(userId)
        }

        setupRecyclerView()

        fetchBlogs()

        binding.addArticleBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddArticleActivity::class.java))
        }

        binding.saveArticleBtn.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }

        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val searchView = binding.searchBlog
        val editText =
            searchView.findViewById<SearchView.SearchAutoComplete>(
                androidx.appcompat.R.id.search_src_text
            )
        editText.setHintTextColor(Color.GRAY)
        editText.setTextColor(Color.BLACK)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })

    }

    private fun fetchBlogs() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                allBlogItems.clear()
                for (snapshot in snapshot.children) {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        blogItems.add(blogItem)
                        allBlogItems.add(blogItem)
                    }
                }
                blogItems.reverse()
                allBlogItems.reverse()
                adapter.notifyDataSetChanged()
                binding.loading.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.loading.visibility = View.GONE
            }
        })
    }


    private fun filter(query: String?) {
        if (query.isNullOrEmpty()) {
            blogItems.clear()
            blogItems.addAll(allBlogItems)
        } else {
            val filteredList = allBlogItems.filter {
                it.heading.contains(query, true) ||
                        it.post.contains(query, true) ||
                        it.userName.contains(query, true)
            }
            blogItems.clear()
            blogItems.addAll(filteredList)
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "users"
            ).child(userId)

        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)

                if (profileImageUrl != null) {
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_user_avatar)
                        .into(binding.profileImage)

                    Log.e("TAG", "onDataChange: $profileImageUrl")
                } else {
                    Log.e("TAG", "Something went wrong")
                }
                binding.loading.visibility = View.GONE

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Error loading profile image ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.loading.visibility = View.GONE

            }

        })
    }

    private fun setupRecyclerView() {
        adapter = BlogAdapter(blogItems, this)
        binding.blogRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        binding.blogRecyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()
    }

}