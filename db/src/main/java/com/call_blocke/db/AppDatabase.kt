package com.call_blocke.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.call_blocke.db.convertor.TaskStatusConvertor
import com.call_blocke.db.entity.*

@Database(
    entities = [TaskEntity::class, ReplayTaskEntity::class, TaskStatusData::class, PhoneNumber::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    TaskStatusConvertor::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun replayTaskDao(): ReplayTaskDao

    abstract fun taskStatusDao(): TaskStatusDao

    abstract fun phoneNumberDao(): PhoneNumberDao
}