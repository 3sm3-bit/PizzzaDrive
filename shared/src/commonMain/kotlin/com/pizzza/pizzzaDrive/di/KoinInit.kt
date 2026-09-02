package com.pizzza.pizzzaDrive.di

import com.pizzza.pizzzaDrive.repository.di.networkModule
import com.pizzza.pizzzaDrive.repository.di.repositoryModule
import com.pizzza.pizzzaDrive.usecases.di.useCasesModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            dispatcherModule,
            repositoryModule,
            networkModule,
            useCasesModule
        )
    }

// Llamada desde iOS
fun initKoin() = initKoin {}
