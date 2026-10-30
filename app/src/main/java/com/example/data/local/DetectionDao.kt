package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionDao {
    @Query("SELECT * FROM detection_history ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectionEntity>>

    @Query("SELECT * FROM detection_history WHERE id = :id LIMIT 1")
    suspend fun getDetectionById(id: Long): DetectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(entity: DetectionEntity): Long

    @Query("DELETE FROM detection_history WHERE id = :id")
    suspend fun deleteDetectionById(id: Long)

    @Query("DELETE FROM detection_history")
    suspend fun clearAllDetections()
}
