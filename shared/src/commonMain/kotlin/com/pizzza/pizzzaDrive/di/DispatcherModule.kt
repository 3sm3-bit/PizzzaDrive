package com.pizzza.pizzzaDrive.di

import com.pizzza.pizzzaDrive.DefaultDispatcherProvider
import com.pizzza.pizzzaDrive.DispatcherProvider
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dispatcherModule = module {
    singleOf(::DefaultDispatcherProvider) { bind<DispatcherProvider>() }
}