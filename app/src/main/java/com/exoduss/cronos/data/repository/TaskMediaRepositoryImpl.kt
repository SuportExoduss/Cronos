package com.exoduss.cronos.data.repository

import com.exoduss.cronos.data.local.dao.TaskMediaDao
import com.exoduss.cronos.data.local.entity.TaskMediaEntity
import com.exoduss.cronos.domain.model.TaskMedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskMediaRepositoryImpl @Inject constructor(
    private val dao: TaskMediaDao
) : TaskMediaRepository {

    override fun getMediaForTask(taskId: String): Flow<List<TaskMedia>> =
        dao.getMediaForTask(taskId).map { list -> list.map { it.toDomain() } }

    override suspend fun addMedia(media: TaskMedia) = dao.insert(media.toEntity())

    override suspend fun deleteMedia(media: TaskMedia) = dao.delete(media.toEntity())

    override suspend fun deleteForTask(taskId: String) = dao.deleteForTask(taskId)

    override suspend fun getPendingBackup(): List<TaskMedia> =
        dao.getPendingBackup().map { it.toDomain() }

    override suspend fun markBackedUp(id: String) = dao.markBackedUp(id)
}

private fun TaskMediaEntity.toDomain() = TaskMedia(id, taskId, uri, mediaType, createdAt, pendingBackup)
private fun TaskMedia.toEntity() = TaskMediaEntity(id, taskId, uri, mediaType, createdAt, pendingBackup)
