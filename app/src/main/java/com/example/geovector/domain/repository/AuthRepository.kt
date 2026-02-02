package com.example.geovector.domain.repository

import com.example.geovector.core.result.AppResult
import com.example.geovector.domain.model.User

interface AuthRepository {
    suspend fun register(fullName: String, email: String, password: String): AppResult<User>
    suspend fun login(email: String, password: String): AppResult<User>
}
