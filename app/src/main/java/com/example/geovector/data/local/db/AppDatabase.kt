package com.example.geovector.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.geovector.data.local.dao.LocationDao
import com.example.geovector.data.local.dao.UserDao
import com.example.geovector.data.local.entity.LocationEntity
import com.example.geovector.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao

}
