package com.call_blocke.db.entity

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    @Insert(onConflict = IGNORE)
    suspend fun save(data: List<TaskEntity>)

    @Update
    suspend fun update(data: TaskEntity)

    @Query("select * from task where confirmAt = 0")
    suspend fun toConfirmList(): List<TaskEntity>

    @Query("select * from task where processAt = 0")
    suspend fun toProcessList(): List<TaskEntity>

    @Query("select * from task order by bufferedAt desc")
    fun taskList(): DataSource.Factory<Int, TaskEntity>

}