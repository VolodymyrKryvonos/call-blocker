package com.call_blocke.app

import android.app.Application
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp

class SmsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SmsBlockerDatabase.init(context = this)
        RepositoryImp.init(this)
    }

}