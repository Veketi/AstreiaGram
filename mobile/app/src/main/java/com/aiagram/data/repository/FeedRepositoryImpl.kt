package com.aiagram.data.repository

import com.aiagram.data.remote.api.FeedApi
import com.aiagram.domain.model.Feed
import com.aiagram.domain.model.Post
import com.aiagram.domain.repository.FeedRepository
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val feedApi: FeedApi
) : FeedRepository {

    override suspend fun getFeed(userId: String, page: Int, limit: Int): Result<Feed> {
        return try {
            val response = feedApi.getFeed(userId, page, limit)
            if (response.isSuccessful) {
                val body = response.body()!!
                val posts = body.posts.map { dto ->
                    Post(
                        id = dto.id,
                        userId = dto.userId,
                        imageUrl = dto.imageUrl,
                        caption = dto.caption,
                        createdAt = dto.createdAt,
                        likeCount = dto.likeCount,
                        commentCount = dto.commentCount
                    )
                }
                Result.success(Feed(body.userId, posts, body.page, body.limit))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao carregar feed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
