package com.aiagram.data.remote.api

import com.aiagram.data.remote.dto.FeedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedApi {

    @GET("api/feed/{userId}")
    suspend fun getFeed(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<FeedResponse>
}
