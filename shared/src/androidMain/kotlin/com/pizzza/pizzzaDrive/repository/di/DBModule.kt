package com.pizzza.pizzzaDrive.repository.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pizzza.pizzzaDrive.repository.db.manager.AppDataBase
import com.pizzza.pizzzaDrive.repository.AndroidConnectivityManager
import com.pizzza.pizzzaDrive.repository.utils.ConnectivityManager
import org.koin.dsl.module

actual val dbModule = module {
    single<ConnectivityManager> { AndroidConnectivityManager(get()) }
    
    single<AppDataBase> {
        val context: Context = get()
        val dbFile = context.getDatabasePath("pizzza_app.db")
        Room.databaseBuilder<AppDataBase>(
            context = context,
            name = dbFile.absolutePath
        )
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
