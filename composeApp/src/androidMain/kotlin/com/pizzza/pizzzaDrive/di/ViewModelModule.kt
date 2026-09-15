package com.pizzza.pizzzaDrive.di

import com.pizzza.pizzzaDrive.repository.AndroidConnectivityManager
import com.pizzza.pizzzaDrive.repository.utils.ConnectivityManager
import com.pizzza.pizzzaDrive.ui.AppViewModel
import com.pizzza.pizzzaDrive.ui.base.BaseViewModel
import com.pizzza.pizzzaDrive.ui.base.GlobalUiStateManager
import com.pizzza.pizzzaDrive.ui.login.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single<ConnectivityManager> { AndroidConnectivityManager(get()) }
    single { GlobalUiStateManager() }
    viewModel { AppViewModel(get(), get()) }
    viewModel { AuthViewModel(get(),get()) }
}
