package com.pizzza.pizzzaDrive.usecases.di

import com.pizzza.pizzzaDrive.usecases.DataUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCasesModule = module {
    factoryOf(::DataUseCase)
}