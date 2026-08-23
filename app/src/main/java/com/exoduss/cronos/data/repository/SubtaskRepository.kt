package com.exoduss.cronos.data.repository

import com.exoduss.cronos.domain.model.Subtask
import kotlinx.coroutines.flow.Flow

interface SubtaskRepository {
    fun getSubtasksForTask(taskId: String): Flow<List<Subtask>>
    suspend fun saveSubtask(subtask: Subtask)
    suspend fun deleteSubtask(subtask: Subtask)
    suspend fun deleteSubtasksForTask(taskId: String)
    suspend fun toggleSubtask(id: String, isDone: Boolean)
}
