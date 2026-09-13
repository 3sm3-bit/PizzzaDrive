package com.pizzza.pizzzaDrive.repository.db.manager

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.ConstructedBy
import androidx.room.RoomDatabaseConstructor
import com.pizzza.pizzzaDrive.repository.db.dao.UserDao
import com.pizzza.pizzzaDrive.repository.db.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1)
@ConstructedBy(AppDataBaseConstructor::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// Room KMP constructor
expect object AppDataBaseConstructor : RoomDatabaseConstructor<AppDataBase>

