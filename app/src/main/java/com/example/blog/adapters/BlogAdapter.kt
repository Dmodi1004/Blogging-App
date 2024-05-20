package com.example.blog.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blog.R
import com.example.blog.databinding.BlogItemBinding
import com.example.blog.models.BlogItemModel
import com.example.blog.views.activities.ArticlesActivity
import com.example.blog.views.activities.ReadMoreActivity
import com.example.blog.views.activities.UserBlogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BlogAdapter(private val items: MutableList<BlogItemModel>, private val context: Context) :
    RecyclerView.Adapter<BlogAdapter.ViewHolder>() {

    val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    val currentUser = FirebaseAuth.getInstance().currentUser

    inner class ViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: BlogItemModel) {

            val postId = model.postId

            binding.apply {
                model.apply {
                    headingTv.text = heading
                    Glide.with(userProfileImage.context)
                        .load(profileImage)
                        .placeholder(R.drawable.ic_user_avatar)
                        .into(userProfileImage)
                    userNameTv.text = userName
                    dateTv.text = date
                    postTv.text = post
                    likeCountTv.text = likeCount.toString()
                }
                readMoreBtn.setOnClickListener {
                    val context = root.context
                    val intent = Intent(context, ReadMoreActivity::class.java)
                    intent.putExtra("blogItem", model)
                    context.startActivity(intent)
                }

                profileLayout.setOnClickListener {
                    val context = root.context

                    if (model.userId == currentUser?.uid) {
                        val intent = Intent(context, ArticlesActivity::class.java)
                        context.startActivity(intent)
                    } else {
                        val intent = Intent(context, UserBlogActivity::class.java)
                        intent.putExtra("blogItem", model)
                        intent.putExtra("userId", model.userId)
                        context.startActivity(intent)
                    }
                }

                val postLikeReference =
                    databaseReference.child("blogs").child(postId).child("likes")
                val currentUserLike = currentUser?.uid?.let { uid ->
                    postLikeReference.child(uid).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likeBtn.setImageResource(R.drawable.ic_heart_fill)
                            } else {
                                binding.likeBtn.setImageResource(R.drawable.ic_heart_black)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }

                likeBtn.setOnClickListener {
                    if (currentUserLike != null) {
                        handleLikeButton(postId, model, binding)
                        notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "You have to login first", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                val userReference = databaseReference.child("users").child(currentUser?.uid ?: "")
                val postSavedReference = userReference.child("savePost").child(postId)

                postSavedReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            binding.postSaveBtn.setImageResource(R.drawable.ic_save_fill)
                        } else {
                            binding.postSaveBtn.setImageResource(R.drawable.ic_save_black)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        val context = binding.root.context
                        Toast.makeText(
                            context,
                            error.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

                postSaveBtn.setOnClickListener {
                    if (currentUserLike != null) {
                        handleSaveButton(postId, model, binding)
                    } else {
                        Toast.makeText(context, "You have to login first", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                postShareBtn.setOnClickListener {
                    sharePost(model)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BlogItemBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val blogItem = items[position]
        holder.bind(model = blogItem)
    }

    override fun getItemCount(): Int = items.size

    private fun handleLikeButton(postId: String, model: BlogItemModel, binding: BlogItemBinding) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).removeValue()
                                model.likedBy?.remove(currentUser.uid)
                                updateLikeButton(binding, false)

                                val newLikeCount = model.likeCount - 1
                                model.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener {
                                Log.e(
                                    "Like",
                                    "onDataChange: Failed to unlike the blog ${it.message}",
                                )
                            }
                    } else {
                        userReference.child("likes").child(postId).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).setValue(true)
                                model.likedBy?.add(currentUser.uid)
                                updateLikeButton(binding, true)

                                val newLikeCount = model.likeCount + 1
                                model.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener {
                                Log.e(
                                    "Like",
                                    "onDataChange: Failed to like the blog ${it.message}",
                                )
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val context = binding.root.context
                    Toast.makeText(
                        context,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateLikeButton(binding: BlogItemBinding, liked: Boolean) {
        if (liked) {
            binding.likeBtn.setImageResource(R.drawable.ic_heart_black)
        } else {
            binding.likeBtn.setImageResource(R.drawable.ic_heart_fill)
        }
    }

    private fun handleSaveButton(postId: String, model: BlogItemModel, binding: BlogItemBinding) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("savePost").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        userReference.child("savePost").child(postId).removeValue()
                            .addOnSuccessListener {
                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = false
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Blog unsaved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(
                                    context,
                                    "Failed to unsaved the Blog",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        binding.postSaveBtn.setImageResource(R.drawable.ic_save_black)
                    } else {
                        userReference.child("savePost").child(postId).setValue(true)
                            .addOnSuccessListener {
                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = true
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(
                                    context,
                                    "Blog saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(
                                    context,
                                    "Failed to save the Blog",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        binding.postSaveBtn.setImageResource(R.drawable.ic_save_fill)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val context = binding.root.context
                    Toast.makeText(
                        context,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun sharePost(model: BlogItemModel) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, model.heading)
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this blog post by: *${model.userName}*\n\n*${model.heading}*\n\n${model.post}\n\nShared via\n*Blog App* created by *Dhruv*"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }

    fun updateData(savedBlogArticles: List<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogArticles)
        notifyDataSetChanged()
    }
}