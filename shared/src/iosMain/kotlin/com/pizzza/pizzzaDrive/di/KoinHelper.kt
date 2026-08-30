package com.pizzza.pizzzaDrive.di

import com.pizzza.pizzzaDrive.usecases.DataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object KoinHelper : KoinComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun getDataUseCase(): DataUseCase = get()
}
