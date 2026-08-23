package com.exoduss.cronos.data.repository

import com.exoduss.cronos.domain.model.TaskMedia
import kotlinx.coroutines.flow.Flow

interface TaskMediaRepository {
    fun getMediaForTask(taskId: String): Flow<List<TaskMedia>>
    suspend fun addMedia(media: TaskMedia)
    suspend fun deleteMedia(media: TaskMedia)
    suspend fun deleteForTask(taskId: String)
    suspend fun getPendingBackup(): List<TaskMedia>
    suspend fun markBackedUp(id: String)
}
