package com.call_blocke.db.entity

import androidx.room.*

@Dao
interface TaskStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskStatus(status: TaskStatusData)

    @Delete
    suspend fun deleteTaskStatus(status: TaskStatusData)

    @Query("SELECT * FROM TaskStatusData")
    suspend fun getAllTaskStatus(): List<TaskStatusData>
}