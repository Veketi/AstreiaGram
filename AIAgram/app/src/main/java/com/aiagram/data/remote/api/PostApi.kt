package com.aiagram.data.remote.api

import com.aiagram.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PostApi {

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponseDto>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: String): Response<PostResponseDto>

    @GET("posts")
    suspend fun getPostsByIds(@Query("ids") ids: List<String>): Response<List<PostResponseDto>>

    @GET("posts/user/{userId}")
    suspend fun getPostsByUserId(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedPostsResponse>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likePost(@Path("id") id: String): Response<Unit>

    @DELETE("posts/{id}/likes")
    suspend fun unlikePost(@Path("id") id: String): Response<Unit>

    @GET("posts/{id}/likes")
    suspend fun getLikes(
        @Path("id") id: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<List<String>>

    @POST("posts/{id}/comments")
    suspend fun addComment(
        @Path("id") id: String,
        @Body request: CommentRequest
    ): Response<Unit>

    @GET("posts/{id}/comments")
    suspend fun getComments(
        @Path("id") id: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<CommentDto>>

    @DELETE("posts/{id}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("id") id: String,
        @Path("commentId") commentId: String
    ): Response<Unit>
}
