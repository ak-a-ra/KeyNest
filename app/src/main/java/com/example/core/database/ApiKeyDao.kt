package com.example.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.core.model.ApiKeyItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys WHERE isDeleted = false ORDER BY isPinned DESC, createdAt DESC")
    fun getAllKeys(): Flow<List<ApiKeyItem>>

    @Query("SELECT * FROM api_keys WHERE isDeleted = false AND id = :id")
    fun getKeyById(id: Long): Flow<ApiKeyItem?>

    @Query("SELECT * FROM api_keys WHERE isDeleted = false AND (title LIKE '%' || :query || '%' OR provider LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY isPinned DESC, createdAt DESC")
    fun searchKeys(query: String): Flow<List<ApiKeyItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(item: ApiKeyItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKeys(items: List<ApiKeyItem>)

    @Update
    suspend fun updateKey(item: ApiKeyItem)

    @Delete
    suspend fun deleteKey(item: ApiKeyItem)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun deleteKeyById(id: Long)

    @Query("UPDATE api_keys SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: Long, isPinned: Boolean)

    @Query("UPDATE api_keys SET copyCount = copyCount + 1, lastCopiedAt = :timestamp WHERE id = :id")
    suspend fun recordCopy(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM api_keys WHERE isDeleted = false")
    fun getKeyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM api_keys WHERE isDeleted = true")
    fun getTrashCount(): Flow<Int>

    @Query("SELECT * FROM api_keys WHERE isDeleted = true ORDER BY createdAt DESC")
    fun getTrashedKeys(): Flow<List<ApiKeyItem>>

    @Query("UPDATE api_keys SET isDeleted = true, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteKey(id: Long, timestamp: Long)

    @Query("UPDATE api_keys SET isDeleted = false, deletedAt = null WHERE id = :id")
    suspend fun restoreKey(id: Long)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun permanentDeleteKey(id: Long)

    @Query("DELETE FROM api_keys WHERE isDeleted = true")
    suspend fun emptyTrash()

    @Query("DELETE FROM api_keys")
    suspend fun deleteAllKeys()

    /** Atomic replace: process death between delete and insert must never leave an empty vault. */
    @Transaction
    suspend fun replaceAllKeys(items: List<ApiKeyItem>) {
        deleteAllKeys()
        insertAllKeys(items)
    }
}
