package com.aiagram.data.repository

import com.aiagram.data.remote.api.PostApi
import com.aiagram.data.remote.dto.CommentRequest
import com.aiagram.data.remote.dto.CreatePostRequest
import com.aiagram.data.remote.dto.PostResponseDto
import com.aiagram.domain.model.Comment
import com.aiagram.domain.model.Post
import com.aiagram.domain.repository.PostRepository
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postApi: PostApi
) : PostRepository {

    private fun mapPost(dto: PostResponseDto) = Post(
        id = dto.id,
        userId = dto.userId,
        imageUrl = dto.imageUrl,
        caption = dto.caption,
        createdAt = dto.createdAt,
        likeCount = dto.likeCount,
        commentCount = dto.commentCount
    )

    override suspend fun createPost(imageUrl: String, caption: String): Result<Post> {
        return try {
            val response = postApi.createPost(CreatePostRequest(imageUrl, caption))
            if (response.isSuccessful) Result.success(mapPost(response.body()!!))
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao criar post"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getPostById(id: String): Result<Post> {
        return try {
            val response = postApi.getPostById(id)
            if (response.isSuccessful) Result.success(mapPost(response.body()!!))
            else Result.failure(Exception(response.errorBody()?.string() ?: "Post não encontrado"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getPostsByUserId(userId: String, page: Int, size: Int): Result<List<Post>> {
        return try {
            val response = postApi.getPostsByUserId(userId, page, size)
            if (response.isSuccessful) {
                Result.success(response.body()!!.content.map { mapPost(it) })
            } else Result.failure(Exception(response.errorBody()?.string() ?: "Erro"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deletePost(id: String): Result<Unit> {
        return try {
            val response = postApi.deletePost(id)
            if (response.isSuccessful || response.code() == 204) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao deletar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun likePost(id: String): Result<Unit> {
        return try {
            val response = postApi.likePost(id)
            if (response.isSuccessful || response.code() == 204) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao curtir"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun unlikePost(id: String): Result<Unit> {
        return try {
            val response = postApi.unlikePost(id)
            if (response.isSuccessful || response.code() == 204) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao descurtir"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getLikes(id: String, page: Int, size: Int): Result<List<String>> {
        return try {
            val response = postApi.getLikes(id, page, size)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun addComment(id: String, content: String): Result<Unit> {
        return try {
            val response = postApi.addComment(id, CommentRequest(content))
            if (response.isSuccessful || response.code() == 204) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao comentar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getComments(id: String, page: Int, size: Int): Result<List<Comment>> {
        return try {
            val response = postApi.getComments(id, page, size)
            if (response.isSuccessful) {
                val comments = response.body()!!.map {
                    Comment(it.id, it.userId, it.content, it.createdAt)
                }
                Result.success(comments)
            } else Result.failure(Exception(response.errorBody()?.string() ?: "Erro"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        return try {
            val response = postApi.deleteComment(postId, commentId)
            if (response.isSuccessful || response.code() == 204) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao deletar comentário"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
