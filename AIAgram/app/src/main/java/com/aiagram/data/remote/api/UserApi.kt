package com.aiagram.data.remote.api

import com.aiagram.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/validate")
    suspend fun validateToken(@Header("Authorization") token: String): Response<ValidateTokenResponse>

    @GET("api/users/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    @PATCH("api/users/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequest): Response<UserProfileResponse>

    @GET("api/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserProfileResponse>

    @GET("api/users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): Response<UserProfileResponse>

    @GET("api/users/{userId}/followers")
    suspend fun getFollowers(@Path("userId") userId: String): Response<FollowListResponse>

    @GET("api/users/{userId}/following")
    suspend fun getFollowing(@Path("userId") userId: String): Response<FollowListResponse>

    @POST("api/users/{userId}/followers")
    suspend fun followUser(@Path("userId") userId: String): Response<Unit>

    @DELETE("api/users/{userId}/followers")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<Unit>
}
