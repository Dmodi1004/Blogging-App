package com.example.blog.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blog.R
import com.example.blog.adapters.ArticleAdapter
import com.example.blog.databinding.ActivityArticlesBinding
import com.example.blog.models.BlogItemModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArticlesActivity : AppCompatActivity() {

    private val binding: ActivityArticlesBinding by lazy {
        ActivityArticlesBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ArticleAdapter

    val currentUserId = auth.currentUser?.uid

    private val EDIT_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        setupRecyclerView()

        databaseReference =
            FirebaseDatabase.getInstance("https://blog-748e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("blogs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogSavedList = ArrayList<BlogItemModel>()
                for (postSnapshot in snapshot.children) {
                    val blogSaved = postSnapshot.getValue(BlogItemModel::class.java)
                    if (blogSaved != null && currentUserId == blogSaved.userId) {
                        blogSavedList.add(blogSaved)
                    }
                }
                adapter.setData(blogSavedList)
                blogSavedList.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ArticlesActivity,
                    "Something went wrong ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    private fun setupRecyclerView() {

        if (currentUserId != null) {

            adapter =
                ArticleAdapter(this, emptyList(), object : ArticleAdapter.OnItemClickListener {

                    override fun onEditClick(blogItem: BlogItemModel) {
                        val intent = Intent(this@ArticlesActivity, EditBlogActivity::class.java)
                        intent.putExtra("blogItem", blogItem)
                        startActivityForResult(intent, EDIT_REQUEST_CODE)
                    }

                    override fun onReadMoreClick(blogItem: BlogItemModel) {
                        val intent = Intent(this@ArticlesActivity, ReadMoreActivity::class.java)
                        intent.putExtra("blogItem", blogItem)
                        startActivity(intent)
                    }

                    override fun onDeleteClick(blogItem: BlogItemModel) {

                        val view = LayoutInflater.from(this@ArticlesActivity)
                            .inflate(R.layout.alert_dialog_box, null)

                        val titleTv = view.findViewById<TextView>(R.id.titleTv)
                        val messageTv = view.findViewById<TextView>(R.id.messageTv)
                        val deleteBtn = view.findViewById<MaterialButton>(R.id.positiveBtn)
                        val cancelBtn = view.findViewById<MaterialButton>(R.id.negativeBtn)

                        val builder = AlertDialog.Builder(this@ArticlesActivity)
                            .setView(view)

                        val dialog = builder.create()

                        titleTv.text = "Delete this post?"
                        messageTv.text =
                            "Are your sure you want to delete this post?\nAfter deleting you can never restore this post,\nit will be permanently deleted."
                        deleteBtn.text = "Delete"
                        cancelBtn.text = "Cancel"

                        deleteBtn.setOnClickListener {
                            deleteBlogPost(blogItem)
                            dialog.dismiss()
                        }
                        cancelBtn.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.show()
                    }
                })
        }

        binding.articleRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        binding.articleRecyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()
    }

    private fun deleteBlogPost(blogItem: BlogItemModel) {
        val postId = blogItem.postId
        val blogPostReference = databaseReference.child(postId)

        blogPostReference.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Post delete successfully", Toast.LENGTH_SHORT).show()
            adapter.notifyDataSetChanged()
        }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong, Please try again", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged()
        }

    }

}