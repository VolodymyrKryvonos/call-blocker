package com.call_blocke.db.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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
    fun taskList(): Flow<List<TaskEntity>>

    @Query("select * from task where id = :id")
    suspend fun findByID(id: Int): TaskEntity?

    @Query("delete from task where processAt = 0")
    suspend fun deleteUnProcessed()

    @Query("SELECT COUNT(deliveredAt) FROM task where deliveredAt between :from and :end and simSlot = :simIndex and status = 'DELIVERED'")
    suspend fun deliveredCountBetweenFoeSim(simIndex: Int, from: Long, end: Long): Int

    @Query("delete from task where simSlot = :simIndex")
    suspend fun clearFor(simIndex: Int)

    @Query("DELETE FROM task WHERE status LIKE 'BUFFERED'")
    suspend fun deleteReceivedMessages()

    @Query("SELECT id FROM task WHERE status LIKE 'BUFFERED'")
    suspend fun getReceivedMessagesID(): List<Int>

    /*@Query("select * from replay_task where rInMsisdn = :rInMsisdn and tText = :tText limit 1")
    suspend fun findReplay(rInMsisdn: String, tText: String): ReplayTaskEntity

    @Query("delete from replay_task")
    suspend fun deleteReplay()*/

}