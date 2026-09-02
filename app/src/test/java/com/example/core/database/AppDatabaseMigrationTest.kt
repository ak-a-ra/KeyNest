package com.example.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate1To2_preservesExistingRows() {
        val dbName = "migration-test-1-2.db"
        helper.createDatabase(dbName, 1).use { v1 ->
            v1.execSQL(
                "INSERT INTO api_keys (title, apiKey, secretKey, provider, category, environment, " +
                    "endpointUrl, organizationId, modelOrProject, notes, tags, isPinned, " +
                    "copyCount, lastCopiedAt, createdAt, expiresAt, rotationDays, colorHex) " +
                    "VALUES ('My Key', 'ak-123', 'sk-456', 'openai', 'ai', 'prod', " +
                    "'https://api.example.com', '', '', 'note', '[]', 1, 3, NULL, 1000, NULL, NULL, '#FF0000')"
            )
        }

        val v2 = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)
        v2.query("SELECT title, apiKey, secretKey, isDeleted, deletedAt FROM api_keys").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("My Key", cursor.getString(0))
            assertEquals("ak-123", cursor.getString(1))
            assertEquals("sk-456", cursor.getString(2))
            assertFalse(cursor.getInt(3) != 0) // isDeleted defaults to false after migration
            assertTrue(cursor.isNull(4)) // deletedAt starts null
            assertFalse(cursor.moveToNext()) // exactly one row survived
        }
        v2.close()
    }

    @Test
    fun migrate2To3_createsProviderProfilesTable() {
        val dbName = "migration-test-2-3.db"
        helper.createDatabase(dbName, 2).use { v2 ->
            v2.execSQL(
                "INSERT INTO api_keys (id, title, apiKey, secretKey, provider, category, environment, " +
                    "endpointUrl, organizationId, modelOrProject, notes, tags, isPinned, isDeleted, " +
                    "deletedAt, copyCount, lastCopiedAt, createdAt, expiresAt, rotationDays, colorHex) " +
                    "VALUES (1, 'OpenAI Key', 'sk-123', '', 'OpenAI', 'AI & LLMs', 'Production', " +
                    "'https://api.openai.com/v1', '', '', '', '[]', 0, 0, NULL, 0, NULL, 1000, NULL, NULL, '#10A37F')"
            )
        }

        val v3 = helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_2_3)
        // Verify provider_profiles table exists and can be queried
        v3.query("SELECT count(*) FROM provider_profiles").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        v3.close()
    }
}
