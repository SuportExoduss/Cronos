// GoogleSignIn legado é deprecated, mas é o caminho prático para obter o escopo
// `drive.appdata` no login. Migração p/ Credential Manager + AuthorizationClient = débito de Fase 5.
@file:Suppress("DEPRECATION")

package com.exoduss.cronos.data.repository

import android.content.Intent
import com.exoduss.cronos.domain.model.CronosUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val prefs: PreferencesRepository
) : AuthRepository {

    override val currentUser: Flow<CronosUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toCronosUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun signInIntent(): Intent = googleSignInClient.signInIntent

    override suspend fun completeSignIn(data: Intent?): Result<CronosUser> = runCatching {
        // Pode lançar ApiException se o usuário cancelar ou o login falhar.
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
        val idToken = account.idToken ?: error("Login Google sem idToken.")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = firebaseAuth.signInWithCredential(credential).await().user?.toCronosUser()
            ?: error("Falha ao autenticar no Firebase.")

        prefs.setGoogleConnected(true)
        // Preenche o nome do perfil a partir da conta Google, se ainda estiver vazio.
        if (prefs.userName.first().isBlank()) {
            user.name?.takeIf { it.isNotBlank() }?.let { prefs.setUserName(it) }
        }
        user
    }

    override suspend fun signOut() {
        // Desconecta do Google e do Firebase; mantém os dados locais intactos.
        runCatching { googleSignInClient.signOut().await() }
        firebaseAuth.signOut()
        prefs.setGoogleConnected(false)
    }

    private fun FirebaseUser.toCronosUser() = CronosUser(
        uid = uid,
        name = displayName,
        email = email,
        photoUrl = photoUrl?.toString()
    )
}
