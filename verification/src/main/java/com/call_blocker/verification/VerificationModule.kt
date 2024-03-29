package com.call_blocker.verification

import com.call_blocker.common.rest.AppRest
import com.call_blocker.verification.data.VerificationRepository
import com.call_blocker.verification.data.api.VerificationApi
import com.call_blocker.verification.domain.SimCardVerificationChecker
import com.call_blocker.verification.domain.SimCardVerificationCheckerImpl
import com.call_blocker.verification.domain.SimCardVerifier
import com.call_blocker.verification.domain.VerificationRepositoryImpl
import org.koin.dsl.module

val verificationModule = module {
    single<VerificationRepository> {
        VerificationRepositoryImpl(
            AppRest(VerificationApi::class.java).build()
        )
    }
    single { SimCardVerifier(get()) }
    single<SimCardVerificationChecker> { SimCardVerificationCheckerImpl(get()) }
}