package com.pizzza.pizzzaDrive.repository.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pizzza.pizzzaDrive.repository.db.manager.AppDataBase
import com.pizzza.pizzzaDrive.repository.db.manager.AppDataBaseConstructor
import com.pizzza.pizzzaDrive.repository.utils.ConnectivityManager
import platform.Foundation.NSHomeDirectory
import org.koin.dsl.module

actual val dbModule = module {
    single<ConnectivityManager> {
        object : ConnectivityManager {
            override fun isConnected(): Boolean = true
        }
    }

    single<AppDataBase> {
        val dbFilePath = NSHomeDirectory() + "/pizzza_drive.db"
        Room.databaseBuilder<AppDataBase>(
            name = dbFilePath,
            factory = { AppDataBaseConstructor.initialize() }
        )
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
