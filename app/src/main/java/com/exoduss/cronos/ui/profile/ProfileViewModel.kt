package com.exoduss.cronos.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.backup.DriveConsentRequiredException
import com.exoduss.cronos.data.profile.ProfilePhotoStore
import com.exoduss.cronos.data.repository.AuthRepository
import com.exoduss.cronos.data.repository.BackupRepository
import com.exoduss.cronos.data.repository.PreferencesRepository
import com.exoduss.cronos.domain.model.CronosUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val backupRepository: BackupRepository,
    private val profilePhotoStore: ProfilePhotoStore
) : ViewModel() {

    /** Foto de perfil em memória (baixada uma única vez no 1º login). */
    private val _profilePhoto = MutableStateFlow<Bitmap?>(null)
    val profilePhoto: StateFlow<Bitmap?> = _profilePhoto.asStateFlow()

    init {
        // Carrega a foto salva (se houver) uma vez, na memória.
        viewModelScope.launch { _profilePhoto.value = profilePhotoStore.loadBitmap() }
    }

    val userName: StateFlow<String> = prefs.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val googleConnected: StateFlow<Boolean> = prefs.googleConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userPlan: StateFlow<String> = prefs.userPlan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gratuito")

    val lastBackupTime: StateFlow<Long> = prefs.lastBackupTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /** Usuário autenticado via Google (null = deslogado). */
    val currentUser: StateFlow<CronosUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()
    fun clearAuthMessage() { _authMessage.value = null }

    // Autorização incremental do Drive: quando o backup/restore exige consentimento,
    // expomos o Intent para a UI lançar; após RESULT_OK, repetimos a ação pendente.
    private val _driveConsentIntent = MutableStateFlow<Intent?>(null)
    val driveConsentIntent: StateFlow<Intent?> = _driveConsentIntent.asStateFlow()
    private var pendingDriveAction: (() -> Unit)? = null
    // true quando o consentimento do Drive faz parte do LOGIN (negar = desfaz o login).
    private var consentForLogin = false

    fun onDriveConsentResult(granted: Boolean) {
        _driveConsentIntent.value = null
        val action = pendingDriveAction
        val wasLogin = consentForLogin
        pendingDriveAction = null
        consentForLogin = false

        if (granted) {
            if (wasLogin) _authMessage.value = "Conectado! Backup no Drive ativado."
            action?.invoke()
        } else if (wasLogin) {
            // Login só se concretiza com as DUAS permissões → sem Drive, desfaz tudo.
            viewModelScope.launch {
                authRepository.signOut()
                profilePhotoStore.delete()
                _profilePhoto.value = null
                _authMessage.value = "É necessário permitir o Google Drive para entrar."
            }
        } else {
            _authMessage.value = "Permissão do Drive não concedida."
        }
    }

    /** Trata falhas das operações de Drive: dispara consentimento ou mostra erro. */
    private fun handleDriveFailure(t: Throwable, retry: () -> Unit) {
        if (t is DriveConsentRequiredException) {
            consentForLogin = false
            pendingDriveAction = retry
            _authMessage.value = null          // sem erro: o diálogo de consentimento vai aparecer
            _driveConsentIntent.value = t.intent
        } else {
            Log.e("CronosBackup", "Falha na operação do Drive", t)
            _authMessage.value = t.message ?: "Falha na operação do Drive."
        }
    }

    /** Intent a ser passado ao launcher de Activity para iniciar o login Google. */
    fun signInIntent(): Intent = authRepository.signInIntent()

    /**
     * Conclui o login Google e, em seguida, EXIGE a permissão do Drive.
     * O login só se concretiza se ambas forem concedidas.
     */
    fun completeSignIn(data: Intent?) {
        viewModelScope.launch {
            authRepository.completeSignIn(data)
                .onSuccess { user ->
                    downloadPhotoOnce(user)
                    requestDriveAfterLogin()
                }
                .onFailure { _authMessage.value = "Login cancelado ou falhou." }
        }
    }

    /** Baixa a foto do Google uma única vez e a mantém em memória. */
    private suspend fun downloadPhotoOnce(user: CronosUser) {
        if (!profilePhotoStore.exists() && !user.photoUrl.isNullOrBlank()) {
            runCatching { profilePhotoStore.downloadAndStore(user.photoUrl) }
        }
        _profilePhoto.value = profilePhotoStore.loadBitmap()
    }

    /** Pede a permissão do Drive logo após o login (parte obrigatória do login). */
    private suspend fun requestDriveAfterLogin() {
        backupRepository.runCatching { ensureDriveAuthorized() }
            .onSuccess { _authMessage.value = "Conectado! Backup no Drive ativado." }
            .onFailure { t ->
                if (t is DriveConsentRequiredException) {
                    consentForLogin = true
                    pendingDriveAction = null          // o próprio consentimento conclui o login
                    _driveConsentIntent.value = t.intent
                } else {
                    _authMessage.value = t.message ?: "Falha ao autorizar o Drive."
                }
            }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            profilePhotoStore.delete()
            _profilePhoto.value = null
            _authMessage.value = "Você saiu da conta Google."
        }
    }

    /** Backup real no Google Drive (appDataFolder). Só grava lastBackupTime se subir com sucesso. */
    fun performBackup() {
        viewModelScope.launch {
            _authMessage.value = "Enviando backup para o Drive..."
            backupRepository.runCatching { backupToDrive() }
                .onSuccess {
                    prefs.setLastBackupTime(System.currentTimeMillis())
                    _authMessage.value = "Backup salvo no Google Drive."
                }
                .onFailure { handleDriveFailure(it) { performBackup() } }
        }
    }

    /** Restaura do Drive (merge não-destrutivo). */
    fun restoreFromDrive() {
        viewModelScope.launch {
            _authMessage.value = "Restaurando do Drive..."
            backupRepository.runCatching { restoreFromDrive() }
                .onSuccess { stats ->
                    _authMessage.value =
                        if (stats == null) "Nenhum backup encontrado no Drive."
                        else "Restaurado: ${stats.tasks} tarefa(s), ${stats.subtasks} subtarefa(s)."
                }
                .onFailure { handleDriveFailure(it) { restoreFromDrive() } }
        }
    }
}
