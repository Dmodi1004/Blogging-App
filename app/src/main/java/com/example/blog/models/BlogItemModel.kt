package com.example.blog.models

import java.io.Serializable

data class BlogItemModel(
    var heading: String = "null",
    val userName: String = "null",
    val date: String = "null",
    var post: String = "null",
    var likeCount: Int = 0,
    val profileImage: String = "null",
    var postId: String = "null",
    val likedBy: MutableList<String>? = null,
    var isSaved: Boolean = false,
    val userId: String? = "null"
): Serializable
