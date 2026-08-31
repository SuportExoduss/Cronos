package com.exoduss.cronos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v5 → v6: adiciona `tasks.nextSpawned` (marca se uma tarefa recorrente já gerou a próxima
 * ocorrência, evitando duplicatas ao desmarcar/marcar concluída de novo).
 *
 * O DEFAULT 0 casa com @ColumnInfo(defaultValue = "0") em TaskEntity — sem isso o Room
 * acusaria divergência de schema em runtime.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN nextSpawned INTEGER NOT NULL DEFAULT 0")
    }
}
