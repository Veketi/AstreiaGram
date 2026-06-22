package com.aiagram.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== AUTH ====================

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("bio") val bio: String? = null
)

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String
)

data class ValidateTokenResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String
)

// ==================== USER ====================

data class UserProfileResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("followerCount") val followerCount: Long,
    @SerializedName("followingCount") val followingCount: Long
)

data class UserSummaryDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatarUrl") val avatarUrl: String?
)

data class FollowListResponse(
    @SerializedName("users") val users: List<UserSummaryDto>,
    @SerializedName("total") val total: Int
)

data class UpdateProfileRequest(
    @SerializedName("bio") val bio: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?
)

// ==================== POST ====================

data class CreatePostRequest(
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("caption") val caption: String
)

data class PostResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("caption") val caption: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("commentCount") val commentCount: Int
)

data class CommentRequest(
    @SerializedName("content") val content: String
)

data class CommentDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String?
)

data class PagedPostsResponse(
    @SerializedName("content") val content: List<PostResponseDto>,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("last") val last: Boolean
)

// ==================== FEED ====================

data class FeedResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("posts") val posts: List<PostResponseDto>,
    @SerializedName("page") val page: Long,
    @SerializedName("limit") val limit: Long
)
