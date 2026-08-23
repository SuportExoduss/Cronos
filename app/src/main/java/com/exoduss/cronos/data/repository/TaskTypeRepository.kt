package com.exoduss.cronos.data.repository

import com.exoduss.cronos.domain.model.TaskType
import kotlinx.coroutines.flow.Flow

interface TaskTypeRepository {
    fun getAllTypes(): Flow<List<TaskType>>
    suspend fun getTypeById(id: String): TaskType?
    suspend fun saveType(type: TaskType)
    suspend fun deleteType(type: TaskType)
    suspend fun getTaskCountForType(typeId: String): Int
}
