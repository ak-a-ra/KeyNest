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
    @Query("SELECT * FROM api_keys ORDER BY isPinned DESC, createdAt DESC")
    fun getAllKeys(): Flow<List<ApiKeyItem>>

    @Query("SELECT * FROM api_keys WHERE id = :id")
    fun getKeyById(id: Long): Flow<ApiKeyItem?>

    @Query("SELECT * FROM api_keys WHERE title LIKE '%' || :query || '%' OR provider LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR environment LIKE '%' || :query || '%' ORDER BY isPinned DESC, createdAt DESC")
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

    @Query("SELECT COUNT(*) FROM api_keys")
    fun getKeyCount(): Flow<Int>
}
