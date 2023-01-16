package com.call_blocke.rest_work_imp

import android.content.Context
import com.call_blocke.db.SmsBlockerDatabase

object RepositoryBuilder {
    fun init(context: Context) {
        SmsBlockerDatabase.init(context)
    }
}