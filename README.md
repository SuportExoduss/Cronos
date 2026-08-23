# Cronos

Super agenda inteligente para Android — tarefas, lembretes notificados e calendário.

> ⚠️ **Repositório privado de backup.** Contém `app/google-services.json` (config do Firebase).
> **NÃO torne este repositório público** sem antes remover esse arquivo.

## Stack
- **Kotlin + Jetpack Compose + Material 3**
- **Hilt** (DI) · **Room** (banco local) · **Navigation Compose** · **DataStore** (preferências)
- **Coroutines/Flow** · **Coil** (imagens) · **Gson** (serialização de backup)
- **Firebase Auth** (login Google) · **Google Drive** (backup do usuário, `appDataFolder`)
- `applicationId` = `com.exoduss.cronos` (debug: `com.exoduss.cronos.debug`)
- minSdk 26 · targetSdk 36

## Funcionalidades já implementadas
- Tarefas com prioridade, status, recorrência (diária/semanal/mensal **materializa a próxima ocorrência**), subtarefas, pin, tipos personalizados, mídia.
- Lembretes via AlarmManager exato (1–12/dia + 3 obrigatórios na véspera) + resumo diário; reagendamento no boot.
- Calendário (dia/semana/mês) com feriados brasileiros.
- Onboarding, perfil, temas claro/escuro.
- **Login Google (Firebase Auth)** + foto de perfil (download único, armazenada localmente).
- **Backup local** (export/import `.json` via SAF) e **backup no Google Drive** do usuário.

## Como compilar
Requer um JDK (o do Android Studio serve):
```bash
# Windows (PowerShell)
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
./gradlew.bat :app:assembleDebug        # gera o APK
./gradlew.bat :app:installDebug         # instala no device conectado
./gradlew.bat :app:testDebugUnitTest    # testes unitários
```

## Arquitetura de backend (custo zero)
- **Login/Perfil:** Firebase Auth (provedor Google).
- **Backup:** Google Drive do próprio usuário (pasta oculta `appDataFolder`) — sem custo de servidor.
- **Planos (futuro):** Google Play Billing.
- Projeto Firebase: **`cronosapp369`**.
  - App base `com.exoduss.cronos` → `1:791739363373:android:3e150f52efc8788ac308a4`
  - App debug `com.exoduss.cronos.debug` → `1:791739363373:android:d349e784825f3814c308a4`

### Recuperar o `google-services.json` (caso ele não esteja presente)
```bash
firebase apps:sdkconfig ANDROID 1:791739363373:android:3e150f52efc8788ac308a4 --project cronosapp369
```
Salve a saída em `app/google-services.json`.

### Notas de recuperação após perda total
1. Clonar este repositório.
2. Criar `local.properties` com `sdk.dir=<caminho do Android SDK>` (o Android Studio recria ao abrir).
3. Se o `google-services.json` faltar, regenerar (comando acima).
4. O **login Google** exige a **SHA-1** do keystore de debug registrada no Firebase.
   Se você trocou de máquina, o keystore de debug muda — gere e registre a nova SHA-1:
   `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   e adicione no Firebase Console (ou `firebase apps:android:sha:create <appId> <sha1> --project cronosapp369`).
