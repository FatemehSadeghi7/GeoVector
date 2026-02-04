package com.example.geovector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.geovector.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("""
    SELECT * FROM users
    WHERE username = :username
      AND passwordHash = :hash
    LIMIT 1
""")
    suspend fun loginByUsernamePassword(username: String, hash: String): UserEntity?
    @Query("UPDATE users SET isLoggedIn = 1 WHERE username = :username")
    suspend fun setLoggedIn(username: String)

    @Query("UPDATE users SET isLoggedIn = 0 WHERE isLoggedIn = 1")
    suspend fun logoutAll()

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM users WHERE isLoggedIn = 1")
    suspend fun deleteLoggedInUser()

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteByUsername(username: String)
}

