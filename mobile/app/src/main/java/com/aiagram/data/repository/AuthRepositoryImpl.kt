package com.aiagram.data.repository

import com.aiagram.data.local.TokenDataStore
import com.aiagram.data.remote.api.UserApi
import com.aiagram.data.remote.dto.ValidateTokenResponse
import com.aiagram.domain.model.AuthData
import com.aiagram.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun register(username: String, email: String, password: String, bio: String?): Result<AuthData> {
        return try {
            val response = userApi.register(
                com.aiagram.data.remote.dto.RegisterRequest(username, email, password, bio)
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                val authData = AuthData(body.token, body.userId, body.username)
                tokenDataStore.saveAuthData(body.token, body.userId, body.username)
                Result.success(authData)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Erro ao registrar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<AuthData> {
        return try {
            val response = userApi.login(
                com.aiagram.data.remote.dto.LoginRequest(username, password)
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                val authData = AuthData(body.token, body.userId, body.username)
                tokenDataStore.saveAuthData(body.token, body.userId, body.username)
                Result.success(authData)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Credenciais inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateToken(token: String): Result<ValidateTokenResponse> {
        return try {
            val response = userApi.validateToken("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Token inválido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
