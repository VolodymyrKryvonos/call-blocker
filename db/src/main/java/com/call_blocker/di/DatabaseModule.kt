package com.call_blocker.di

import com.call_blocker.db.SmsBlockerDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { SmsBlockerDatabase(androidContext()) }
}