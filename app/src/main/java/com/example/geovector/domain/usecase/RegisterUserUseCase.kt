package com.example.geovector.domain.usecase

import com.example.geovector.domain.repository.AuthRepository

class RegisterUserUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(fullName: String, email: String, password: String) =
        repo.register(fullName, email, password)
}
