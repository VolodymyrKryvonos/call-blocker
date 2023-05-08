package com.call_blocker.db.convertor

import androidx.room.TypeConverter
import com.call_blocker.db.entity.TaskStatus

class TaskStatusConvertor {

    @TypeConverter
    fun to(data: String) = enumValueOf<TaskStatus>(data)

    @TypeConverter
    fun from(data: TaskStatus) = data.name

}