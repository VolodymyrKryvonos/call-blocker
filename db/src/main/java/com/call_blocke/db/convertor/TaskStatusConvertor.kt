package com.call_blocke.db.convertor

import androidx.room.TypeConverter
import com.call_blocke.db.entity.TaskStatus

class TaskStatusConvertor {

    @TypeConverter
    fun to(data: String) = enumValueOf<TaskStatus>(data)

    @TypeConverter
    fun from(data: TaskStatus) = data.name

}