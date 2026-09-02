package com.example.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Query("SELECT * FROM provider_profiles WHERE isDeleted = false ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllProviders(): Flow<List<ProviderProfileEntity>>

    @Query("SELECT * FROM provider_profiles WHERE isDeleted = false AND id = :id")
    fun getProviderById(id: String): Flow<ProviderProfileEntity?>

    @Query("SELECT * FROM provider_profiles WHERE isDeleted = true ORDER BY deletedAt DESC")
    fun getTrashedProviders(): Flow<List<ProviderProfileEntity>>

    @Query("SELECT COUNT(*) FROM provider_profiles WHERE isDeleted = false")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM provider_profiles WHERE isDeleted = true")
    fun getTrashCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(item: ProviderProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProviders(items: List<ProviderProfileEntity>)

    @Update
    suspend fun updateProvider(item: ProviderProfileEntity)

    @Delete
    suspend fun deleteProvider(item: ProviderProfileEntity)

    @Query("DELETE FROM provider_profiles WHERE id = :id")
    suspend fun deleteProviderById(id: String)

    @Query("UPDATE provider_profiles SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)

    @Query("UPDATE provider_profiles SET isActive = :isActive WHERE id = :id")
    suspend fun toggleActive(id: String, isActive: Boolean)

    @Query("UPDATE provider_profiles SET copyCount = copyCount + 1, lastCopiedAt = :timestamp WHERE id = :id")
    suspend fun recordCopy(id: String, timestamp: Long)

    @Query("UPDATE provider_profiles SET isDeleted = true, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteProvider(id: String, timestamp: Long)

    @Query("UPDATE provider_profiles SET isDeleted = false, deletedAt = null WHERE id = :id")
    suspend fun restoreProvider(id: String)

    @Query("DELETE FROM provider_profiles WHERE id = :id")
    suspend fun permanentDeleteProvider(id: String)

    @Query("DELETE FROM provider_profiles WHERE isDeleted = true")
    suspend fun emptyTrash()

    @Query("DELETE FROM provider_profiles")
    suspend fun deleteAllProviders()

    @Transaction
    suspend fun replaceAllProviders(items: List<ProviderProfileEntity>) {
        deleteAllProviders()
        insertAllProviders(items)
    }
}
