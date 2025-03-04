package com.google.android.samples.socialite.data.repository

import com.google.android.samples.socialite.model.AlbumDetails
import com.google.android.samples.socialite.model.Comment
import com.google.android.samples.socialite.model.EventDetails
import com.google.android.samples.socialite.model.MerchDetails
import com.google.android.samples.socialite.model.Post
import com.google.android.samples.socialite.model.PostType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor() {

    // Sample posts for testing
    private val samplePosts = listOf(
        Post(
            id = "post1",
            authorId = "user1", // John Lennon
            content = "Just finished writing a new song! Can't wait to share it with all of you soon!",
            type = PostType.GENERAL_UPDATE
        ),
        Post(
            id = "post2",
            authorId = "user3", // Dave Grohl
            content = "Excited to announce our upcoming world tour! Tickets go on sale next week.",
            type = PostType.EVENT_ANNOUNCEMENT,
            eventDetails = EventDetails(
                eventName = "Foo Fighters World Tour 2025",
                location = "Multiple Venues",
                date = Calendar.getInstance().apply {
                    add(Calendar.MONTH, 2)
                }.timeInMillis,
                description = "Join us for an unforgettable experience as we tour the world!",
                ticketLink = "https://example.com/tickets"
            )
        ),
        Post(
            id = "post3",
            authorId = "user4", // Taylor Swift
            content = "Just released my new album 'Midnight Melodies'. Stream it now on all platforms!",
            type = PostType.NEW_RELEASE,
            albumDetails = AlbumDetails(
                albumName = "Midnight Melodies",
                releaseDate = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                trackCount = 12,
                streamingLinks = mapOf(
                    "Spotify" to "https://spotify.com/album/example",
                    "Apple Music" to "https://music.apple.com/album/example"
                )
            )
        ),
        Post(
            id = "post4",
            authorId = "user5", // Billie Eilish
            content = "Check out our new merchandise collection! Limited edition items available now.",
            type = PostType.MERCHANDISE,
            merchDetails = MerchDetails(
                itemName = "Limited Edition Tour Hoodie",
                price = 59.99,
                description = "Exclusive hoodie featuring artwork from the latest tour.",
                shopLink = "https://shop.example.com/hoodie",
                isLimitedEdition = true
            )
        ),
        Post(
            id = "post5",
            authorId = "user2", // Paul McCartney
            content = "In the studio today working on something special. Here's a sneak peek!",
            type = PostType.BEHIND_THE_SCENES
        )
    )

    private val _posts = MutableStateFlow<List<Post>>(samplePosts)
    val posts = _posts.asStateFlow()

    fun getAllPosts(): Flow<List<Post>> {
        return _posts.map { posts ->
            posts.sortedWith(
                compareByDescending<Post> { it.isPinned }
                    .thenByDescending { it.timestamp }
            )
        }
    }

    fun getPublicPosts(): Flow<List<Post>> {
        return _posts.map { posts ->
            posts.filter { !it.isAdminOnly }
                .sortedWith(
                    compareByDescending<Post> { it.isPinned }
                        .thenByDescending { it.timestamp }
                )
        }
    }

    fun getPostsByAuthor(authorId: String): Flow<List<Post>> {
        return _posts.map { posts ->
            posts.filter { it.authorId == authorId }
                .sortedByDescending { it.timestamp }
        }
    }

    fun getPostsByType(type: PostType): Flow<List<Post>> {
        return _posts.map { posts ->
            posts.filter { it.type == type }
                .sortedByDescending { it.timestamp }
        }
    }

    fun getEventPosts(): Flow<List<Post>> {
        return _posts.map { posts ->
            posts.filter { it.eventDetails != null }
                .sortedBy { it.eventDetails?.date }
        }
    }

    fun getAdminPosts(): Flow<List<Post>> {
        return _posts.map { posts ->
            posts.filter { it.isAdminOnly }
                .sortedByDescending { it.timestamp }
        }
    }

    suspend fun createPost(
        authorId: String,
        content: String,
        imageUrl: String? = null,
        type: PostType = PostType.GENERAL_UPDATE,
        eventDetails: EventDetails? = null,
        albumDetails: AlbumDetails? = null,
        merchDetails: MerchDetails? = null,
        isAdminOnly: Boolean = false,
        isPinned: Boolean = false
    ): Post {
        val post = Post(
            authorId = authorId,
            content = content,
            imageUrl = imageUrl,
            type = type,
            eventDetails = eventDetails,
            albumDetails = albumDetails,
            merchDetails = merchDetails,
            isAdminOnly = isAdminOnly,
            isPinned = isPinned
        )

        val currentPosts = _posts.value.toMutableList()
        currentPosts.add(post)
        _posts.value = currentPosts

        return post
    }

    suspend fun updatePost(postId: String, updatedPost: Post) {
        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) updatedPost else post
        }
        _posts.value = updatedPosts
    }

    suspend fun addComment(postId: String, authorId: String, content: String): Comment {
        val comment = Comment(
            authorId = authorId,
            content = content
        )

        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.toMutableList()
                updatedComments.add(comment)
                post.copy(comments = updatedComments)
            } else {
                post
            }
        }

        _posts.value = updatedPosts
        return comment
    }

    suspend fun likePost(postId: String, userId: String) {
        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) {
                val likesList = post.likes.toMutableList()
                if (!likesList.contains(userId)) {
                    likesList.add(userId)
                }
                post.copy(likes = likesList)
            } else {
                post
            }
        }

        _posts.value = updatedPosts
    }

    suspend fun unlikePost(postId: String, userId: String) {
        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) {
                val likesList = post.likes.toMutableList()
                likesList.remove(userId)
                post.copy(likes = likesList)
            } else {
                post
            }
        }

        _posts.value = updatedPosts
    }

    suspend fun likeComment(postId: String, commentId: String, userId: String) {
        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        val likesList = comment.likes.toMutableList()
                        if (!likesList.contains(userId)) {
                            likesList.add(userId)
                        }
                        comment.copy(likes = likesList)
                    } else {
                        comment
                    }
                }
                post.copy(comments = updatedComments)
            } else {
                post
            }
        }

        _posts.value = updatedPosts
    }

    suspend fun pinPost(postId: String, isPinned: Boolean) {
        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) post.copy(isPinned = isPinned) else post
        }

        _posts.value = updatedPosts
    }

    suspend fun deletePost(postId: String) {
        val updatedPosts = _posts.value.filterNot { it.id == postId }
        _posts.value = updatedPosts
    }
}
