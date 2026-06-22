package com.aiagram.data.repository

import com.aiagram.data.remote.api.UserApi
import com.aiagram.data.remote.dto.UpdateProfileRequest
import com.aiagram.domain.model.User
import com.aiagram.domain.model.UserSummary
import com.aiagram.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    private fun mapUser(dto: com.aiagram.data.remote.dto.UserProfileResponse) = User(
        id = dto.id,
        username = dto.username,
        email = dto.email,
        bio = dto.bio,
        avatarUrl = dto.avatarUrl,
        createdAt = dto.createdAt,
        followerCount = dto.followerCount,
        followingCount = dto.followingCount
    )

    override suspend fun getMyProfile(): Result<User> {
        return try {
            val response = userApi.getMyProfile()
            if (response.isSuccessful) Result.success(mapUser(response.body()!!))
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateMyProfile(bio: String?, avatarUrl: String?): Result<User> {
        return try {
            val response = userApi.updateMyProfile(UpdateProfileRequest(bio, avatarUrl))
            if (response.isSuccessful) Result.success(mapUser(response.body()!!))
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao atualizar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val response = userApi.getUserById(userId)
            if (response.isSuccessful) Result.success(mapUser(response.body()!!))
            else Result.failure(Exception(response.errorBody()?.string() ?: "Usuário não encontrado"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getUserByUsername(username: String): Result<User> {
        return try {
            val response = userApi.getUserByUsername(username)
            if (response.isSuccessful) Result.success(mapUser(response.body()!!))
            else Result.failure(Exception(response.errorBody()?.string() ?: "Usuário não encontrado"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getFollowers(userId: String): Result<List<UserSummary>> {
        return try {
            val response = userApi.getFollowers(userId)
            if (response.isSuccessful) {
                val users = response.body()!!.users.map {
                    UserSummary(it.id, it.username, it.avatarUrl)
                }
                Result.success(users)
            } else Result.failure(Exception(response.errorBody()?.string() ?: "Erro"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getFollowing(userId: String): Result<List<UserSummary>> {
        return try {
            val response = userApi.getFollowing(userId)
            if (response.isSuccessful) {
                val users = response.body()!!.users.map {
                    UserSummary(it.id, it.username, it.avatarUrl)
                }
                Result.success(users)
            } else Result.failure(Exception(response.errorBody()?.string() ?: "Erro"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun followUser(userId: String): Result<Unit> {
        return try {
            val response = userApi.followUser(userId)
            if (response.isSuccessful || response.code() == 204) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao seguir"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun unfollowUser(userId: String): Result<Unit> {
        return try {
            val response = userApi.unfollowUser(userId)
            if (response.isSuccessful || response.code() == 204) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao deixar de seguir"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
