package com.exoduss.cronos.data.repository

/** Estatística de um restore — quantos registros vieram no backup. */
data class BackupStats(
    val tasks: Int,
    val subtasks: Int,
    val taskTypes: Int,
    val media: Int
)

/**
 * Backup completo dos dados locais.
 *
 * [export] produz o JSON (destino agnóstico: arquivo local via SAF ou Google Drive).
 * [import] aplica um JSON sobre o banco local com semântica de MERGE não-destrutivo
 * (REPLACE por id): sobrescreve registros coincidentes e adiciona novos, mas NUNCA
 * apaga dados locais ausentes no backup — evita perda acidental em restore parcial.
 */
interface BackupRepository {
    suspend fun export(): String
    suspend fun import(json: String): BackupStats

    /** Garante a autorização do Drive (dispara consentimento se faltar). Usado após o login. */
    suspend fun ensureDriveAuthorized()

    /** Sobe o backup completo para o Google Drive do usuário (appDataFolder). */
    suspend fun backupToDrive()

    /** Restaura do Drive (merge não-destrutivo). Retorna null se não há backup na nuvem. */
    suspend fun restoreFromDrive(): BackupStats?
}
