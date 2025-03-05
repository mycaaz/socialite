package com.bandconnect.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bandconnect.models.Post
import com.bandconnect.models.Comment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PostViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadPosts()
    }
    
    private fun loadPosts() {
        _isLoading.value = true
        database.child("posts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    postSnapshot.getValue(Post::class.java)?.let { post ->
                        postList.add(post)
                    }
                }
                _posts.value = postList.sortedByDescending { it.timestamp as Long }
                _isLoading.value = false
            }
            
            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        })
    }
    
    fun createPost(content: String, userId: String, mediaUrl: String? = null, mediaType: String? = null) {
        val postId = UUID.randomUUID().toString()
        val post = Post(
            id = postId,
            userId = userId,
            content = content,
            mediaUrl = mediaUrl,
            mediaType = mediaType
        )
        database.child("posts").child(postId).setValue(post)
    }
    
    fun likePost(postId: String, userId: String) {
        val postRef = database.child("posts").child(postId).child("likes")
        postRef.child(userId).setValue(true)
    }
    
    fun unlikePost(postId: String, userId: String) {
        val postRef = database.child("posts").child(postId).child("likes")
        postRef.child(userId).removeValue()
    }
    
    fun addComment(postId: String, userId: String, content: String) {
        val commentId = UUID.randomUUID().toString()
        val comment = Comment(
            id = commentId,
            userId = userId,
            content = content
        )
        database.child("posts").child(postId).child("comments")
            .child(commentId).setValue(comment)
    }
}