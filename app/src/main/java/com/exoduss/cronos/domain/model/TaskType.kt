package com.exoduss.cronos.domain.model

data class TaskType(
    val id: String,
    val name: String,
    val color: String,
    val icon: String,
    val isDefault: Boolean = false
)

val DefaultTaskTypes = listOf(
    TaskType("trabalho", "Trabalho", "#2E6DB4", "work", true),
    TaskType("pessoal", "Pessoal", "#27AE60", "person", true),
    TaskType("saude", "Saúde", "#C0392B", "favorite", true),
    TaskType("estudo", "Estudo", "#8E44AD", "school", true),
    TaskType("familia", "Família", "#E67E22", "family_restroom", true)
)
