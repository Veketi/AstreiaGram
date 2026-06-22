package com.aiagram.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String?,
    val bio: String?,
    val avatarUrl: String?,
    val createdAt: String?,
    val followerCount: Long,
    val followingCount: Long
)

data class UserSummary(
    val id: String,
    val username: String,
    val avatarUrl: String?
)

data class Post(
    val id: String,
    val userId: String,
    val imageUrl: String,
    val caption: String?,
    val createdAt: String?,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByMe: Boolean = false
)

data class Comment(
    val id: String,
    val userId: String,
    val content: String,
    val createdAt: String?
)

data class Feed(
    val userId: String,
    val posts: List<Post>,
    val page: Long,
    val limit: Long
)

data class AuthData(
    val token: String,
    val userId: String,
    val username: String
)
