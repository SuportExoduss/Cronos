package com.exoduss.cronos.data.repository

import com.exoduss.cronos.data.local.dao.TaskTypeDao
import com.exoduss.cronos.data.local.entity.toEntity
import com.exoduss.cronos.domain.model.DefaultTaskTypes
import com.exoduss.cronos.domain.model.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskTypeRepositoryImpl @Inject constructor(
    private val taskTypeDao: TaskTypeDao
) : TaskTypeRepository {

    override fun getAllTypes(): Flow<List<TaskType>> =
        taskTypeDao.getAllTypes().map { it.map { e -> e.toDomain() } }

    override suspend fun getTypeById(id: String): TaskType? =
        taskTypeDao.getTypeById(id)?.toDomain()

    override suspend fun saveType(type: TaskType) =
        taskTypeDao.insertType(type.toEntity())

    override suspend fun deleteType(type: TaskType) =
        taskTypeDao.deleteType(type.toEntity())

    override suspend fun getTaskCountForType(typeId: String): Int =
        taskTypeDao.getTaskCountForType(typeId)
}
