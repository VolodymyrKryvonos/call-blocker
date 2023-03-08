package com.call_blocke.db.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query

@Dao
interface PhoneNumberDao {

    @Insert(onConflict = IGNORE)
    suspend fun addNumber(phone: PhoneNumber)

    @Query("DELETE FROM PhoneNumber")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM PhoneNumber WHERE phoneNumber=:phone")
    suspend fun isExist(phone: String): Int
}
