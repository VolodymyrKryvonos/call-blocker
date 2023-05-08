package com.call_blocker.rest_work_imp

import android.content.Context
import com.call_blocker.db.SmsBlockerDatabase

object RepositoryBuilder {
    fun init(context: Context) {
        SmsBlockerDatabase.init(context)
    }
}