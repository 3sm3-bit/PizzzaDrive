package com.pizzza.pizzzaDrive.application

import android.app.Application
import com.pizzza.pizzzaDrive.di.initKoin
import com.pizzza.pizzzaDrive.di.viewModelModule
import com.pizzza.pizzzaDrive.utils.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class PizzaApplication: Application()  {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        initKoin {
            androidContext(this@PizzaApplication)
            androidLogger()
            modules(viewModelModule)
        }
    }
}
