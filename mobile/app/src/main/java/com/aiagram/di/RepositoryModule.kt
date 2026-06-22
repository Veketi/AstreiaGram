package com.aiagram.di

import com.aiagram.data.repository.AuthRepositoryImpl
import com.aiagram.data.repository.FeedRepositoryImpl
import com.aiagram.data.repository.PostRepositoryImpl
import com.aiagram.data.repository.UserRepositoryImpl
import com.aiagram.domain.repository.AuthRepository
import com.aiagram.domain.repository.FeedRepository
import com.aiagram.domain.repository.PostRepository
import com.aiagram.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository
}
