package com.call_blocker.a_repository.di

import com.call_blocker.a_repository.repository.LogRepositoryImpl
import com.call_blocker.a_repository.repository.ReplyRepositoryImpl
import com.call_blocker.a_repository.repository.SettingsRepositoryImp
import com.call_blocker.a_repository.repository.TaskRepositoryImp
import com.call_blocker.a_repository.repository.UserRepositoryImp
import com.call_blocker.a_repository.repository.UssdRepositoryImpl
import com.call_blocker.a_repository.rest.LogRest
import com.call_blocker.a_repository.rest.ReplyRest
import com.call_blocker.a_repository.rest.SettingsRest
import com.call_blocker.a_repository.rest.TaskRest
import com.call_blocker.a_repository.rest.UserRest
import com.call_blocker.a_repository.rest.UssdRest
import com.call_blocker.common.rest.AppRest
import com.call_blocker.rest_work_imp.LogRepository
import com.call_blocker.rest_work_imp.ReplyRepository
import com.call_blocker.rest_work_imp.SettingsRepository
import com.call_blocker.rest_work_imp.TaskRepository
import com.call_blocker.rest_work_imp.UserRepository
import com.call_blocker.rest_work_imp.UssdRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.dsl.module

val repositoryModule = module {
    single { AppRest(LogRest::class.java).build() }
    single { AppRest(ReplyRest::class.java).build() }
    single { AppRest(SettingsRest::class.java).build() }
    single { AppRest(TaskRest::class.java).build() }
    single { AppRest(UserRest::class.java).build() }
    single { AppRest(UssdRest::class.java).build() }

    single { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }

    single<LogRepository> { LogRepositoryImpl(get()) }
    single<ReplyRepository> { ReplyRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImp(get(), get()) }
    single<TaskRepository> { TaskRepositoryImp(get(), get(), get()) }
    single<UserRepository> { UserRepositoryImp(get(), get()) }
    single<UssdRepository> { UssdRepositoryImpl(get()) }
}