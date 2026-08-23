package com.exoduss.cronos.data.backup

import com.exoduss.cronos.data.local.entity.SubtaskEntity
import com.exoduss.cronos.data.local.entity.TaskEntity
import com.exoduss.cronos.data.local.entity.TaskMediaEntity
import com.exoduss.cronos.data.local.entity.TaskTypeEntity

/**
 * Formato serializável de um backup completo do Cronos.
 *
 * `version` versiona o FORMATO do backup (independente da versão do schema Room).
 * Ao mudar a forma deste JSON de modo incompatível, incremente CURRENT_VERSION e
 * trate as versões antigas no [BackupSerializer]/[com.exoduss.cronos.data.repository.BackupRepository].
 *
 * Os logs de lembrete (reminder_logs) NÃO são incluídos: são efêmeros e reconstruídos
 * pelo agendador a partir das tarefas restauradas.
 */
data class BackupData(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val tasks: List<TaskEntity> = emptyList(),
    val subtasks: List<SubtaskEntity> = emptyList(),
    val taskTypes: List<TaskTypeEntity> = emptyList(),
    val media: List<TaskMediaEntity> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}
