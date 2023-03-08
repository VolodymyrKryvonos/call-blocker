package com.call_blocke.db.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query

@Dao
interface ReplayTaskDao {

    @Insert(onConflict = IGNORE)
    suspend fun save(data: List<ReplayTaskEntity>)

    @Query("select rInMsisdn from replay_task")
    suspend fun rInPhoneList(): List<String>

    @Query("select * from replay_task where rInMsisdn = :rInMsisdn and tText = :rTextReply")
    suspend fun find(rInMsisdn: String, rTextReply: String): ReplayTaskEntity?

    @Query("delete from replay_task")
    suspend fun deleteAll()

}