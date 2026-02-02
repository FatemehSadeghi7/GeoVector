package com.example.geovector.domain.usecase

import com.example.geovector.domain.repository.AuthRepository

class LoginUserUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        repo.login(email, password)
}
