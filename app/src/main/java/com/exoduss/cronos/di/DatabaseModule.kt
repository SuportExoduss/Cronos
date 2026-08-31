package com.exoduss.cronos.di

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.exoduss.cronos.data.local.CronosDatabase
import com.exoduss.cronos.data.local.MIGRATION_5_6
import com.exoduss.cronos.data.local.dao.ReminderLogDao
import com.exoduss.cronos.data.local.dao.SubtaskDao
import com.exoduss.cronos.data.local.dao.TaskDao
import com.exoduss.cronos.data.local.dao.TaskMediaDao
import com.exoduss.cronos.data.local.dao.TaskTypeDao
import com.exoduss.cronos.domain.model.DefaultTaskTypes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CronosDatabase =
        Room.databaseBuilder(context, CronosDatabase::class.java, "cronos.db")
            .addMigrations(MIGRATION_5_6)
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    DefaultTaskTypes.forEach { type ->
                        val cv = ContentValues().apply {
                            put("id",        type.id)
                            put("name",      type.name)
                            put("color",     type.color)
                            put("icon",      type.icon)
                            put("isDefault", 1)
                        }
                        db.insert("task_types", SQLiteDatabase.CONFLICT_IGNORE, cv)
                    }
                }
            })
            .build()

    @Provides @Singleton fun provideTaskDao(db: CronosDatabase): TaskDao         = db.taskDao()
    @Provides @Singleton fun provideTaskTypeDao(db: CronosDatabase): TaskTypeDao = db.taskTypeDao()
    @Provides @Singleton fun provideSubtaskDao(db: CronosDatabase): SubtaskDao   = db.subtaskDao()
    @Provides @Singleton fun provideReminderLogDao(db: CronosDatabase): ReminderLogDao = db.reminderLogDao()
    @Provides @Singleton fun provideTaskMediaDao(db: CronosDatabase): TaskMediaDao     = db.taskMediaDao()
}
