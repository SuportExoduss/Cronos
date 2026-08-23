package com.exoduss.cronos.data.repository

import androidx.room.withTransaction
import com.exoduss.cronos.data.backup.BackupData
import com.exoduss.cronos.data.backup.BackupSerializer
import com.exoduss.cronos.data.backup.DriveBackupDataSource
import com.exoduss.cronos.data.local.CronosDatabase
import com.exoduss.cronos.data.local.dao.SubtaskDao
import com.exoduss.cronos.data.local.dao.TaskDao
import com.exoduss.cronos.data.local.dao.TaskMediaDao
import com.exoduss.cronos.data.local.dao.TaskTypeDao
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: CronosDatabase,
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val taskTypeDao: TaskTypeDao,
    private val taskMediaDao: TaskMediaDao,
    private val serializer: BackupSerializer,
    private val driveDataSource: DriveBackupDataSource
) : BackupRepository {

    override suspend fun export(): String {
        val data = BackupData(
            tasks     = taskDao.getAllSnapshot(),
            subtasks  = subtaskDao.getAllSnapshot(),
            taskTypes = taskTypeDao.getAllSnapshot(),
            media     = taskMediaDao.getAllSnapshot()
        )
        return serializer.toJson(data)
    }

    override suspend fun import(json: String): BackupStats {
        // fromJson valida formato/versão ANTES de tocar no banco — falha cedo, sem corromper.
        val data = serializer.fromJson(json)

        // Operação atômica: ou aplica tudo, ou nada (rollback automático em exceção).
        database.withTransaction {
            taskTypeDao.upsertTypes(data.taskTypes)            // REPLACE
            data.tasks.forEach { taskDao.upsertTask(it) }       // REPLACE
            data.subtasks.forEach { subtaskDao.upsertSubtask(it) } // REPLACE
            data.media.forEach { taskMediaDao.insert(it) }      // REPLACE
        }

        return BackupStats(
            tasks     = data.tasks.size,
            subtasks  = data.subtasks.size,
            taskTypes = data.taskTypes.size,
            media     = data.media.size
        )
    }

    override suspend fun ensureDriveAuthorized() {
        driveDataSource.ensureAuthorized()
    }

    override suspend fun backupToDrive() {
        driveDataSource.upload(export())
    }

    override suspend fun restoreFromDrive(): BackupStats? {
        val json = driveDataSource.download() ?: return null
        return import(json)
    }
}
