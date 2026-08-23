package com.exoduss.cronos.data.repository

import android.content.Intent
import com.exoduss.cronos.domain.model.CronosUser
import kotlinx.coroutines.flow.Flow

/**
 * Autenticação via Google (Firebase Auth).
 *
 * O login pede também o escopo `drive.appdata` — assim a MESMA sessão habilita o
 * backup no Google Drive do usuário (1.1b-ii), sem um segundo consentimento.
 */
interface AuthRepository {
    /** Emite o usuário atual (null = deslogado). Reage a login/logout. */
    val currentUser: Flow<CronosUser?>

    /** Intent a ser lançado pela UI para iniciar o fluxo de Google Sign-In. */
    fun signInIntent(): Intent

    /** Conclui o login a partir do resultado do Intent. */
    suspend fun completeSignIn(data: Intent?): Result<CronosUser>

    suspend fun signOut()
}
