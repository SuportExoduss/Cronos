// GoogleSignIn.getLastSignedInAccount é deprecated (ver nota em AuthRepositoryImpl) — mantido p/ escopo Drive.
@file:Suppress("DEPRECATION")

package com.exoduss.cronos.data.backup

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lê/grava o backup no **Google Drive do próprio usuário**, na pasta oculta `appDataFolder`
 * (privada ao app, não aparece no Drive do usuário e não consome a aparência da conta).
 *
 * Usa REST v3 direto via HttpURLConnection para evitar a dependência pesada
 * `google-api-services-drive`. O token OAuth vem da conta já logada (escopo consentido no login).
 *
 * IMPORTANTE: este código foi validado por COMPILAÇÃO; o round-trip real só pode ser
 * confirmado em um device com conta Google + Drive API ativa.
 */
/**
 * Sinaliza que o usuário precisa conceder a permissão do Google Drive (autorização incremental).
 * [intent] deve ser lançado pela UI; após RESULT_OK, repetir a operação de backup/restore.
 */
class DriveConsentRequiredException(val intent: Intent) :
    Exception("Permissão do Google Drive necessária.")

@Singleton
class DriveBackupDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Garante que o escopo do Drive está autorizado. Lança [DriveConsentRequiredException]
     * se faltar consentimento (para a UI lançar o diálogo). Usado logo após o login.
     */
    suspend fun ensureAuthorized(): Unit = withContext(Dispatchers.IO) {
        accessToken() // dispara o consentimento se necessário; token descartado
    }

    /** Sobe o JSON, criando o arquivo na 1ª vez e sobrescrevendo nas seguintes. */
    suspend fun upload(json: String): Unit = withContext(Dispatchers.IO) {
        val token = accessToken()
        val fileId = findFileId(token) ?: createFile(token)
        val res = request(
            url = "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media",
            method = "PATCH",
            token = token,
            contentType = "application/json; charset=UTF-8",
            payload = json.toByteArray(Charsets.UTF_8)
        )
        check(res.code in 200..299) { "Falha ao enviar backup ao Drive (HTTP ${res.code}): ${res.body.take(180)}" }
    }

    /** Baixa o JSON do backup, ou null se ainda não existe nenhum. */
    suspend fun download(): String? = withContext(Dispatchers.IO) {
        val token = accessToken()
        val fileId = findFileId(token) ?: return@withContext null
        val res = request(
            url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media",
            method = "GET",
            token = token
        )
        check(res.code in 200..299) { "Falha ao baixar backup do Drive (HTTP ${res.code}): ${res.body.take(180)}" }
        res.body
    }

    // ── internos ─────────────────────────────────────────────────────────────

    private fun accessToken(): String {
        val account = GoogleSignIn.getLastSignedInAccount(context)?.account
            ?: throw IllegalStateException("Conecte sua conta Google antes de usar o backup na nuvem.")
        return try {
            GoogleAuthUtil.getToken(context, account, "oauth2:$DRIVE_APPDATA_SCOPE")
        } catch (e: UserRecoverableAuthException) {
            // Autorização incremental: usuário ainda não consentiu o Drive → propaga o Intent
            // de consentimento para a UI lançar; depois a ação é repetida automaticamente.
            Log.w(TAG, "Drive requer consentimento do usuário", e)
            throw DriveConsentRequiredException(e.intent ?: throw e)
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao obter token OAuth do Drive (conta=${account.name})", e)
            throw e
        }
    }

    private fun findFileId(token: String): String? {
        val q = URLEncoder.encode("name = '$FILE_NAME'", "UTF-8")
        val res = request(
            url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&fields=files(id,name)&q=$q",
            method = "GET",
            token = token
        )
        check(res.code in 200..299) { "Falha ao consultar o Drive (HTTP ${res.code}): ${res.body.take(180)}" }
        val files = JSONObject(res.body).optJSONArray("files")
        return if (files != null && files.length() > 0) files.getJSONObject(0).getString("id") else null
    }

    private fun createFile(token: String): String {
        val metadata = JSONObject()
            .put("name", FILE_NAME)
            .put("parents", JSONArray().put("appDataFolder"))
            .toString()
        val res = request(
            url = "https://www.googleapis.com/drive/v3/files?fields=id",
            method = "POST",
            token = token,
            contentType = "application/json; charset=UTF-8",
            payload = metadata.toByteArray(Charsets.UTF_8)
        )
        check(res.code in 200..299) { "Falha ao criar arquivo no Drive (HTTP ${res.code}): ${res.body.take(180)}" }
        return JSONObject(res.body).getString("id")
    }

    private data class HttpResult(val code: Int, val body: String)

    private fun request(
        url: String,
        method: String,
        token: String,
        contentType: String? = null,
        payload: ByteArray? = null
    ): HttpResult {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            // HttpURLConnection não suporta PATCH nativamente → override aceito pelas APIs do Google.
            if (method == "PATCH") {
                conn.requestMethod = "POST"
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            } else {
                conn.requestMethod = method
            }
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 30_000
            conn.readTimeout = 30_000
            contentType?.let { conn.setRequestProperty("Content-Type", it) }
            if (payload != null) {
                conn.doOutput = true
                conn.outputStream.use { it.write(payload) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) Log.e(TAG, "Drive HTTP $code @ $method $url -> ${body.take(500)}")
            return HttpResult(code, body)
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        private const val TAG = "CronosBackup"
        private const val FILE_NAME = "cronos-backup.json"
        private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }
}
