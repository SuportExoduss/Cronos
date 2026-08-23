package com.exoduss.cronos.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda a foto de perfil do Google no armazenamento interno do app.
 *
 * Baixada UMA única vez (no primeiro login) e lida da memória/disco local depois —
 * sem cache HTTP e sem re-download a cada abertura.
 */
@Singleton
class ProfilePhotoStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val file: File get() = File(context.filesDir, FILE_NAME)

    fun exists(): Boolean = file.exists()

    /** Baixa e grava a foto (best-effort em resolução maior). */
    suspend fun downloadAndStore(photoUrl: String): Unit = withContext(Dispatchers.IO) {
        // URLs do Google terminam em "=s96-c"; pedimos um tamanho maior quando possível.
        val url = photoUrl.replace(Regex("=s\\d+(-c)?$"), "=s256-c")
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 20_000
            conn.readTimeout = 20_000
            conn.inputStream.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        } finally {
            conn.disconnect()
        }
    }

    /** Decodifica a foto salva para um Bitmap em memória (null se não houver). */
    suspend fun loadBitmap(): Bitmap? = withContext(Dispatchers.IO) {
        if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    fun delete() {
        if (file.exists()) file.delete()
    }

    companion object {
        private const val FILE_NAME = "profile_photo.jpg"
    }
}
