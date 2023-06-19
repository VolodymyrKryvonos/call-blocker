package com.call_blocker.verification

import com.call_blocker.common.rest.AppRest
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.data.api.VerificationApi
import com.call_blocker.verification.domain.SimCardVerifier
import com.call_blocker.verification.domain.VerificationRepositoryImpl
import org.koin.dsl.module

val verificationModule = module {
    single<VerificationRepository> {
        VerificationRepositoryImpl(
            AppRest(VerificationApi::class.java).build(),
            get()
        )
    }
    single { SimCardVerifier(get()) }
}