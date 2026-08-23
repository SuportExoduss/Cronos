package com.exoduss.cronos.domain.model

/** Usuário autenticado via Google (Firebase Auth). Dados puxados da conta Google no login. */
data class CronosUser(
    val uid: String,
    val name: String?,
    val email: String?,
    val photoUrl: String?
)
