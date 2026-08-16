package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApiKeyItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys WHERE isDeleted = false ORDER BY isPinned DESC, createdAt DESC")
    fun getAllKeys(): Flow<List<ApiKeyItem>>

    @Query("SELECT * FROM api_keys WHERE isDeleted = false AND id = :id")
    fun getKeyById(id: Long): Flow<ApiKeyItem?>

    @Query("SELECT * FROM api_keys WHERE isDeleted = false AND (title LIKE '%' || :query || '%' OR provider LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR environment LIKE '%' || :query || '%') ORDER BY isPinned DESC, createdAt DESC")
    fun searchKeys(query: String): Flow<List<ApiKeyItem>>

    @Query("SELECT COUNT(*) FROM api_keys WHERE isDeleted = false")
    fun getKeyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM api_keys WHERE isDeleted = true")
    fun getTrashCount(): Flow<Int>

    @Query("SELECT * FROM api_keys WHERE isDeleted = true ORDER BY createdAt DESC")
    fun getTrashedKeys(): Flow<List<ApiKeyItem>>

    @Update
    suspend fun updateKey(item: ApiKeyItem)

    @Query("UPDATE api_keys SET isDeleted = true, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteKey(id: Long, timestamp: Long)

    @Query("UPDATE api_keys SET isDeleted = false, deletedAt = null WHERE id = :id")
    suspend fun restoreKey(id: Long)

    @Query("UPDATE api_keys SET isDeleted = true WHERE id = :id")
    suspend fun permanentDeleteKey(id: Long)
}
