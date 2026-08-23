package com.exoduss.cronos.di

import com.exoduss.cronos.data.repository.AuthRepository
import com.exoduss.cronos.data.repository.AuthRepositoryImpl
import com.exoduss.cronos.data.repository.BackupRepository
import com.exoduss.cronos.data.repository.BackupRepositoryImpl
import com.exoduss.cronos.data.repository.SubtaskRepository
import com.exoduss.cronos.data.repository.SubtaskRepositoryImpl
import com.exoduss.cronos.data.repository.TaskMediaRepository
import com.exoduss.cronos.data.repository.TaskMediaRepositoryImpl
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.data.repository.TaskRepositoryImpl
import com.exoduss.cronos.data.repository.TaskTypeRepository
import com.exoduss.cronos.data.repository.TaskTypeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds @Singleton
    abstract fun bindTaskTypeRepository(impl: TaskTypeRepositoryImpl): TaskTypeRepository

    @Binds @Singleton
    abstract fun bindSubtaskRepository(impl: SubtaskRepositoryImpl): SubtaskRepository

    @Binds @Singleton
    abstract fun bindTaskMediaRepository(impl: TaskMediaRepositoryImpl): TaskMediaRepository

    @Binds @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
