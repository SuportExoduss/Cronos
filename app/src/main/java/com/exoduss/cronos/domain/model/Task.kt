package com.exoduss.cronos.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val typeId: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val pinned: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderFrequency: Int = 1,
    val reminderDaysBefore: Int = 0,
    val isGroupTask: Boolean = false,
    val groupId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Priority(val label: String) {
    LOW("Baixa"),
    MEDIUM("Média"),
    HIGH("Alta")
}

enum class TaskStatus(val label: String) {
    PENDING("Pendente"),
    IN_PROGRESS("Em progresso"),
    DONE("Concluída"),
    CANCELLED("Cancelada")
}

enum class Recurrence(val label: String) {
    NONE("Não repete"),
    DAILY("Diária"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal")
}

fun Task.isOverdue(): Boolean {
    val date = dueDate ?: return false
    return date < LocalDate.now() && status != TaskStatus.DONE && status != TaskStatus.CANCELLED
}

fun Task.isActiveToday(): Boolean =
    dueDate == LocalDate.now()
        && status != TaskStatus.DONE
        && status != TaskStatus.CANCELLED

fun Task.smartGroup(): Int {
    val today = LocalDate.now()
    return when {
        status == TaskStatus.DONE || status == TaskStatus.CANCELLED -> 5
        pinned -> 0
        dueDate != null && dueDate < today -> 1
        dueDate == today -> 2
        dueDate == null -> 3
        else -> 4
    }
}

/**
 * Gera a PRÓXIMA ocorrência de uma tarefa recorrente, com base na sua data de vencimento.
 *
 * Retorna null quando não há recorrência (NONE) ou quando a tarefa não tem [dueDate]
 * (sem âncora temporal não há como avançar). A nova ocorrência é uma cópia PENDENTE,
 * com novo id e timestamps, preservando tipo/prioridade/recorrência/configuração de lembrete.
 *
 * Usado ao concluir uma tarefa recorrente para materializar a próxima — antes a recorrência
 * era declarada mas nunca acontecia (feature fantasma).
 */
fun Task.nextOccurrence(now: Long = System.currentTimeMillis()): Task? {
    val base = dueDate ?: return null
    val next = when (recurrence) {
        Recurrence.NONE    -> return null
        Recurrence.DAILY   -> base.plusDays(1)
        Recurrence.WEEKLY  -> base.plusWeeks(1)
        Recurrence.MONTHLY -> base.plusMonths(1)
    }
    return copy(
        id        = UUID.randomUUID().toString(),
        dueDate   = next,
        status    = TaskStatus.PENDING,
        createdAt = now,
        updatedAt = now
    )
}
