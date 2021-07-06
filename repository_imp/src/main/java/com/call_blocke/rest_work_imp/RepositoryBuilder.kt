package com.call_blocke.rest_work_imp

import android.annotation.SuppressLint
import android.content.Context
import com.call_blocke.db.SmsBlockerDatabase

@SuppressLint("StaticFieldLeak")
object RepositoryBuilder {

    lateinit var mContext: Context

    fun init(context: Context) {
        SmsBlockerDatabase.init(context)
        this.mContext = context
    }

}