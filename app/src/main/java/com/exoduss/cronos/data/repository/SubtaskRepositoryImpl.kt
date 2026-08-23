package com.exoduss.cronos.data.repository

import com.exoduss.cronos.data.local.dao.SubtaskDao
import com.exoduss.cronos.data.local.entity.toEntity
import com.exoduss.cronos.domain.model.Subtask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubtaskRepositoryImpl @Inject constructor(
    private val subtaskDao: SubtaskDao
) : SubtaskRepository {

    override fun getSubtasksForTask(taskId: String): Flow<List<Subtask>> =
        subtaskDao.getSubtasksForTask(taskId).map { it.map { e -> e.toDomain() } }

    override suspend fun saveSubtask(subtask: Subtask) =
        subtaskDao.upsertSubtask(subtask.toEntity())

    override suspend fun deleteSubtask(subtask: Subtask) =
        subtaskDao.deleteSubtask(subtask.toEntity())

    override suspend fun deleteSubtasksForTask(taskId: String) =
        subtaskDao.deleteSubtasksForTask(taskId)

    override suspend fun toggleSubtask(id: String, isDone: Boolean) =
        subtaskDao.toggleSubtask(id, isDone)
}
