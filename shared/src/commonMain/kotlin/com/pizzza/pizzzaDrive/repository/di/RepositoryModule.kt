package com.pizzza.pizzzaDrive.repository.di

import com.pizzza.pizzzaDrive.repository.network.DataNetwork
import com.pizzza.pizzzaDrive.usecases.network.IDataNetwork
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::DataNetwork) { bind<IDataNetwork>() }
}
