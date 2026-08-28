package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.model.ApiKeyItem

@Database(entities = [ApiKeyItem::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apiKeyDao(): ApiKeyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "keynest_vault.db"
            )
                .addMigrations(*ALL_MIGRATIONS)
                .build()
            INSTANCE = instance
            instance
        }
        
    }
}
