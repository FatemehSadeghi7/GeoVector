package com.example.geovector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.geovector.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(point: LocationEntity)

    @Query("SELECT * FROM location WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsBySession(sessionId: Long): Flow<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE username = :username ORDER BY timestamp ASC")
    fun getPointsByUsername(username: String): Flow<List<LocationEntity>>

    @Query("SELECT DISTINCT sessionId FROM location WHERE username = :username ORDER BY sessionId DESC")
    fun getSessionsByUsername(username: String): Flow<List<Long>>

    @Query("DELETE FROM location")
    suspend fun deleteAll()

    @Query("DELETE FROM location WHERE username = :username")
    suspend fun deleteByUsername(username: String)


}