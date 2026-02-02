package com.example.geovector.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val age: Int,
    val birthDateMillis: Long,   // تاریخ تولد به میلی‌ثانیه (epoch)
    val username: String,
    val passwordHash: String
)
