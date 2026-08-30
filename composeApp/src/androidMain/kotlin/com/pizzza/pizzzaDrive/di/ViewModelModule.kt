package com.pizzza.pizzzaDrive.di

import com.pizzza.pizzzaDrive.ui.AppViewModel
import com.pizzza.pizzzaDrive.ui.base.BaseViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AppViewModel(get(), get()) }
    viewModel { BaseViewModel(get()) }
}
