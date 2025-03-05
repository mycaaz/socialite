package com.bandconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bandconnect.R
import com.bandconnect.models.Post
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private var posts: List<Post> = listOf(),
    private val currentUserId: String,
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.postContent)
        private val mediaImage: ImageView = itemView.findViewById(R.id.postMedia)
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButton)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(post: Post) {
            contentText.text = post.content

            // Handle media content
            if (post.mediaUrl != null && post.mediaType == "image") {
                mediaImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.mediaUrl)
                    .into(mediaImage)
            } else {
                mediaImage.visibility = View.GONE
            }

            // Handle likes
            val isLiked = post.likes[currentUserId] == true
            likeButton.setImageResource(
                if (isLiked) R.drawable.ic_liked
                else R.drawable.ic_like
            )
            likesCount.text = post.likes.size.toString()

            // Handle comments
            commentsCount.text = post.comments.size.toString()

            // Format timestamp
            val date = Date(post.timestamp as Long)
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            timestamp.text = formatter.format(date)

            // Click listeners
            likeButton.setOnClickListener { onLikeClick(post) }
            commentButton.setOnClickListener { onCommentClick(post) }
        }
    }
}