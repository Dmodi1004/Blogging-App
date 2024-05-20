package com.example.blog.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blog.R
import com.example.blog.databinding.ArticleItemBinding
import com.example.blog.models.BlogItemModel

class ArticleAdapter(
    private val context: Context,
    private var blogList: List<BlogItemModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(blogItem: BlogItemModel)
        fun onReadMoreClick(blogItem: BlogItemModel)
        fun onDeleteClick(blogItem: BlogItemModel)
    }

    inner class ViewHolder(private val binding: ArticleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blogItem: BlogItemModel) {

            binding.apply {
                blogItem.apply {
                    headingTv.text = heading
                    Glide.with(userProfileImage.context)
                        .load(profileImage)
                        .placeholder(R.drawable.ic_user_avatar)
                        .into(userProfileImage)
                    userNameTv.text = userName
                    dateTv.text = date
                    postTv.text = post
                }

                readMoreBtn.setOnClickListener {
                    itemClickListener.onReadMoreClick(blogItem)
                }

                editBtn.setOnClickListener {
                    itemClickListener.onEditClick(blogItem)
                }

                deleteBtn.setOnClickListener {
                    itemClickListener.onDeleteClick(blogItem)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ArticleItemBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val blogItem = blogList[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int = blogList.size
    fun setData(blogSavedList: ArrayList<BlogItemModel>) {
        this.blogList = blogSavedList
        notifyDataSetChanged()
    }
}