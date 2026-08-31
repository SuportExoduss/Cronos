package com.exoduss.cronos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.exoduss.cronos.data.local.dao.ReminderLogDao
import com.exoduss.cronos.data.local.dao.SubtaskDao
import com.exoduss.cronos.data.local.dao.TaskDao
import com.exoduss.cronos.data.local.dao.TaskMediaDao
import com.exoduss.cronos.data.local.dao.TaskTypeDao
import com.exoduss.cronos.data.local.entity.ReminderLogEntity
import com.exoduss.cronos.data.local.entity.SubtaskEntity
import com.exoduss.cronos.data.local.entity.TaskEntity
import com.exoduss.cronos.data.local.entity.TaskMediaEntity
import com.exoduss.cronos.data.local.entity.TaskTypeEntity

// ── Como criar uma Migration ───────────────────────────────────────────────────
//
// 1. Altere a entidade (@Entity) correspondente.
// 2. Incremente `version` em 1 aqui.
// 3. Crie um objeto de migração no arquivo MigrationN_M.kt (ex.: Migration5to6.kt):
//
//      val MIGRATION_5_6 = object : Migration(5, 6) {
//          override fun migrate(db: SupportSQLiteDatabase) {
//              db.execSQL("ALTER TABLE tasks ADD COLUMN newColumn TEXT")
//          }
//      }
//
// 4. Registre no DatabaseModule.provideDatabase:
//      .addMigrations(MIGRATION_5_6)
//
// 5. Execute o app — Room valida o esquema exportado em schemas/version.json.
//    Se a migração estiver incompleta, o Room lança IllegalStateException em desenvolvimento.
//
// NUNCA use .fallbackToDestructiveMigration() em produção — apaga dados do usuário.
// ─────────────────────────────────────────────────────────────────────────────

@Database(
    entities = [
        TaskEntity::class,
        TaskTypeEntity::class,
        SubtaskEntity::class,
        ReminderLogEntity::class,
        TaskMediaEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class CronosDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskTypeDao(): TaskTypeDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun reminderLogDao(): ReminderLogDao
    abstract fun taskMediaDao(): TaskMediaDao
}
