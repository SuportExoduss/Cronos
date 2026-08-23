package com.exoduss.cronos.data.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converte [BackupData] de/para JSON.
 *
 * Camada PURA (sem Android/Room) — testável em unit tests JVM. O Gson é usado por ser
 * uma única dependência sem plugin e funcionar tanto no app quanto nos testes locais.
 */
@Singleton
class BackupSerializer @Inject constructor() {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    fun toJson(data: BackupData): String = gson.toJson(data)

    /**
     * @throws IllegalArgumentException se o JSON for inválido, vazio ou de uma versão
     *         de formato mais nova do que esta build sabe ler.
     */
    fun fromJson(json: String): BackupData {
        val data = try {
            gson.fromJson(json, BackupData::class.java)
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException("Arquivo de backup corrompido ou inválido.", e)
        } ?: throw IllegalArgumentException("Arquivo de backup vazio ou inválido.")

        require(data.version <= BackupData.CURRENT_VERSION) {
            "Backup criado por uma versão mais recente do app (formato ${data.version})."
        }
        return data
    }
}
