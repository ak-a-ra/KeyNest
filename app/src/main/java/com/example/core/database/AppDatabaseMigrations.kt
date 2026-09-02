package com.example.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: adds soft-delete columns `isDeleted` (NOT NULL) and `deletedAt` (nullable).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_api_keys` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`apiKey` TEXT NOT NULL, " +
                "`secretKey` TEXT NOT NULL, " +
                "`provider` TEXT NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`environment` TEXT NOT NULL, " +
                "`endpointUrl` TEXT NOT NULL, " +
                "`organizationId` TEXT NOT NULL, " +
                "`modelOrProject` TEXT NOT NULL, " +
                "`notes` TEXT NOT NULL, " +
                "`tags` TEXT NOT NULL, " +
                "`isPinned` INTEGER NOT NULL, " +
                "`isDeleted` INTEGER NOT NULL, " +
                "`deletedAt` INTEGER, " +
                "`copyCount` INTEGER NOT NULL, " +
                "`lastCopiedAt` INTEGER, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`expiresAt` INTEGER, " +
                "`rotationDays` INTEGER, " +
                "`colorHex` TEXT NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO `_new_api_keys` (" +
                "`id`,`title`,`apiKey`,`secretKey`,`provider`,`category`,`environment`," +
                "`endpointUrl`,`organizationId`,`modelOrProject`,`notes`,`tags`,`isPinned`," +
                "`isDeleted`,`deletedAt`,`copyCount`,`lastCopiedAt`,`createdAt`," +
                "`expiresAt`,`rotationDays`,`colorHex`) " +
                "SELECT `id`,`title`,`apiKey`,`secretKey`,`provider`,`category`,`environment`," +
                "`endpointUrl`,`organizationId`,`modelOrProject`,`notes`,`tags`,`isPinned`," +
                "0,NULL,`copyCount`,`lastCopiedAt`,`createdAt`," +
                "`expiresAt`,`rotationDays`,`colorHex` FROM `api_keys`"
        )
        db.execSQL("DROP TABLE `api_keys`")
        db.execSQL("ALTER TABLE `_new_api_keys` RENAME TO `api_keys`")
    }
}

/**
 * v2 -> v3: creates Agora-style `provider_profiles` table for multi-key provider profiles.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `provider_profiles` (" +
                "`id` TEXT PRIMARY KEY NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`displayName` TEXT NOT NULL, " +
                "`baseUrl` TEXT NOT NULL, " +
                "`customHeadersJson` TEXT NOT NULL, " +
                "`isActive` INTEGER NOT NULL, " +
                "`keysJson` TEXT NOT NULL, " +
                "`activeKeyId` TEXT NOT NULL, " +
                "`isPinned` INTEGER NOT NULL, " +
                "`isDeleted` INTEGER NOT NULL, " +
                "`deletedAt` INTEGER, " +
                "`copyCount` INTEGER NOT NULL, " +
                "`lastCopiedAt` INTEGER, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, " +
                "`colorHex` TEXT NOT NULL, " +
                "`notes` TEXT NOT NULL, " +
                "`tags` TEXT NOT NULL)"
        )
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
