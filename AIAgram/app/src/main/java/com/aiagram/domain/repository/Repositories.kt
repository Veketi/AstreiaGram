package com.aiagram.domain.repository

import com.aiagram.data.remote.dto.*
import com.aiagram.domain.model.*

interface AuthRepository {
    suspend fun register(username: String, email: String, password: String, bio: String?): Result<AuthData>
    suspend fun login(username: String, password: String): Result<AuthData>
    suspend fun validateToken(token: String): Result<ValidateTokenResponse>
}

interface UserRepository {
    suspend fun getMyProfile(): Result<User>
    suspend fun updateMyProfile(bio: String?, avatarUrl: String?): Result<User>
    suspend fun getUserById(userId: String): Result<User>
    suspend fun getUserByUsername(username: String): Result<User>
    suspend fun getFollowers(userId: String): Result<List<UserSummary>>
    suspend fun getFollowing(userId: String): Result<List<UserSummary>>
    suspend fun followUser(userId: String): Result<Unit>
    suspend fun unfollowUser(userId: String): Result<Unit>
}

interface PostRepository {
    suspend fun createPost(imageUrl: String, caption: String): Result<Post>
    suspend fun getPostById(id: String): Result<Post>
    suspend fun getPostsByUserId(userId: String, page: Int, size: Int): Result<List<Post>>
    suspend fun deletePost(id: String): Result<Unit>
    suspend fun likePost(id: String): Result<Unit>
    suspend fun unlikePost(id: String): Result<Unit>
    suspend fun getLikes(id: String, page: Int, size: Int): Result<List<String>>
    suspend fun addComment(id: String, content: String): Result<Unit>
    suspend fun getComments(id: String, page: Int, size: Int): Result<List<Comment>>
    suspend fun deleteComment(postId: String, commentId: String): Result<Unit>
}

interface FeedRepository {
    suspend fun getFeed(userId: String, page: Int, limit: Int): Result<Feed>
}
