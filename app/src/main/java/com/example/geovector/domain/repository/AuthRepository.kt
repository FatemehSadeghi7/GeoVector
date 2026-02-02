package com.example.geovector.domain.repository

import com.example.geovector.core.result.AppResult
import com.example.geovector.domain.model.User

interface AuthRepository {
    suspend fun register(
        fullName: String,
        age: Int,
        birthDateMillis: Long,
        username: String,
        password: String
    ): AppResult<User>

    suspend fun login(
        username: String,
        password: String
    ): AppResult<User>
}
