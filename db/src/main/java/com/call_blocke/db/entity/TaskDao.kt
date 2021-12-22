package com.call_blocke.db.entity

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    @Insert(onConflict = REPLACE)
    suspend fun save(data: List<TaskEntity>)

    @Insert(onConflict = REPLACE)
    suspend fun save(data: TaskEntity)

    @Update
    suspend fun update(data: TaskEntity)

    @Query("select * from task where confirmAt = 0")
    suspend fun toConfirmList(): List<TaskEntity>

    @Query("select * from task where processAt = 0")
    suspend fun toProcessList(): List<TaskEntity>

    @Query("select * from task order by bufferedAt desc")
    fun taskList(): DataSource.Factory<Int, TaskEntity>

    @Query("select * from task where id = :id")
    suspend fun findByID(id: Int): TaskEntity?

    @Query("delete from task where processAt = 0")
    suspend fun deleteUnProcessed()

    @Query("SELECT COUNT(deliveredAt) FROM task where deliveredAt between :from and :end and simSlot = :simIndex and status = 'DELIVERED'")
    suspend fun deliveredCountBetweenFoeSim(simIndex: Int, from: Long, end: Long): Int

    @Query("delete from task where simSlot = :simIndex")
    suspend fun clearFor(simIndex: Int)

    /*@Query("select * from replay_task where rInMsisdn = :rInMsisdn and tText = :tText limit 1")
    suspend fun findReplay(rInMsisdn: String, tText: String): ReplayTaskEntity

    @Query("delete from replay_task")
    suspend fun deleteReplay()*/

}