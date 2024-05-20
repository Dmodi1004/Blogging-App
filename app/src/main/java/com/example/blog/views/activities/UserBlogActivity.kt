package com.example.blog.views.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blog.R
import com.example.blog.adapters.UserBlogAdapter
import com.example.blog.databinding.ActivityUserBlogBinding
import com.example.blog.models.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/*
class UserBlogActivity : AppCompatActivity() {

    private val binding: ActivityUserBlogBinding by lazy {
        ActivityUserBlogBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: BlogAdapter
    private val blogItems = mutableListOf<BlogItemModel>()

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val blogs = intent.getSerializableExtra("blogItem") as BlogItemModel

        binding.apply {

            if (blogs != null) {
                blogs.apply {

                    userNameTv.text = userName
                    Glide.with(this@UserBlogActivity)
                        .load(profileImage)
                        .into(userProfileImage)
                }
            } else {
                Toast.makeText(this@UserBlogActivity, "Failed to load data", Toast.LENGTH_SHORT)
                    .show()
            }

            backBtn.setOnClickListener { finish() }

            auth = FirebaseAuth.getInstance()
            databaseReference =
                FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("blogs")

            setupRecyclerView()

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blogItems.clear()
                    for (snapshot in snapshot.children) {
                        val blogItem = snapshot.getValue(BlogItemModel::class.java)
                        if (blogItem != null) {
                            blogItems.add(blogItem)
                        }
                    }
                    blogItems.reverse()
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@UserBlogActivity,
                        "Something went wrong ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        }

    }

    private fun setupRecyclerView() {
        adapter = BlogAdapter(blogItems, this)
        binding.userBlogRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        binding.userBlogRecyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()
    }

}*/


class UserBlogActivity : AppCompatActivity() {

    private val binding: ActivityUserBlogBinding by lazy {
        ActivityUserBlogBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: UserBlogAdapter
    private val blogItems = mutableListOf<BlogItemModel>()
    private val allBlogItems = mutableListOf<BlogItemModel>()

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val blogs = intent.getSerializableExtra("blogItem") as BlogItemModel
        val userId = intent.getStringExtra("userId") ?: return

        binding.apply {
            backBtn.setOnClickListener { finish() }

            auth = FirebaseAuth.getInstance()
            databaseReference =
                FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                    "blogs"
                )

            if (blogs != null) {
                blogs.apply {
                    userNameTv.text = userName
                    Glide.with(this@UserBlogActivity)
                        .load(profileImage)
                        .placeholder(R.drawable.ic_user_avatar)
                        .into(userProfileImage)
                }
            } else {
                Toast.makeText(this@UserBlogActivity, "Failed to load data", Toast.LENGTH_SHORT)
                    .show()
            }

            setupRecyclerView()

            fetchUserPosts(userId)

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
    }

    private fun setupRecyclerView() {
        adapter = UserBlogAdapter(blogItems, this)
        binding.userBlogRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        binding.userBlogRecyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()
    }

    private fun fetchUserPosts(userId: String) {
        databaseReference.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blogItems.clear()
                    allBlogItems.clear()
                    for (snapshot in snapshot.children) {
                        val blogItem = snapshot.getValue(BlogItemModel::class.java)
                        if (blogItem != null) {
                            blogItems.add(blogItem)
                            allBlogItems.add(blogItem)
                            Log.e("blogItems", "$blogItems")
                        }
                    }
                    blogItems.reverse()
                    allBlogItems.reverse()
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@UserBlogActivity,
                        "Something went wrong ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
                        it.post.contains(query, true)
            }
            blogItems.clear()
            blogItems.addAll(filteredList)
        }
        adapter.notifyDataSetChanged()
    }

}
