package com.example.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: adds soft-delete columns `isDeleted` (NOT NULL) and `deletedAt` (nullable).
 *
 * Table rebuild instead of ALTER TABLE ADD COLUMN because Room validates the resulting
 * schema against the exported 2.json, where `isDeleted` has NO default value — SQLite
 * requires one on ADD COLUMN NOT NULL, so a plain ALTER would fail validation.
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

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
