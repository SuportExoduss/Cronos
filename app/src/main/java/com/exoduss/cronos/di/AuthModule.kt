// GoogleSignIn legado (deprecated) — necessário p/ escopo Drive. Ver nota em AuthRepositoryImpl.
@file:Suppress("DEPRECATION")

package com.exoduss.cronos.di

import android.content.Context
import com.exoduss.cronos.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // default_web_client_id é gerado pelo plugin google-services a partir do google-services.json.
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            // Autorização incremental: o escopo do Drive NÃO é pedido aqui — login fica rápido.
            // O Drive é autorizado sob demanda no 1º backup (ver DriveBackupDataSource).
            .build()
        return GoogleSignIn.getClient(context, options)
    }
}
