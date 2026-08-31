package com.exoduss.cronos.data.media

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda as mídias das tarefas no armazenamento INTERNO do app (`filesDir/media`).
 *
 * Antes, as mídias ficavam como URIs `content://` (galeria) ou em `externalCacheDir` (câmera):
 *  - `content://` da galeria não persistia (e chegava a crashar no takePersistableUriPermission);
 *  - `externalCacheDir` pode ser limpo pelo sistema → as fotos sumiam.
 *
 * Copiando os bytes para dentro do app, a mídia é permanente, sempre legível (file://) e
 * fica disponível para futuro upload ao Drive.
 */
@Singleton
class TaskMediaStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dir: File
        get() = File(context.filesDir, "media").apply { if (!exists()) mkdirs() }

    /** Copia o conteúdo de [source] para um arquivo interno e devolve um `file://` URI estável. */
    suspend fun importFromUri(source: Uri, mediaType: String): String = withContext(Dispatchers.IO) {
        val ext = if (mediaType == "video") "mp4" else "jpg"
        val dest = File(dir, "${UUID.randomUUID()}.$ext")
        context.contentResolver.openInputStream(source)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Não foi possível ler a mídia selecionada.")
        Uri.fromFile(dest).toString()
    }

    /** Remove o arquivo interno referenciado por um `file://` URI (ignora URIs externas). */
    fun deleteFile(uriString: String) {
        runCatching {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") uri.path?.let { File(it).takeIf(File::exists)?.delete() }
        }
    }
}
