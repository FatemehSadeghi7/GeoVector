package com.example.geovector.presentation.screens.register

import RegisterUserUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegisterViewModelFactory(
    private val registerUseCase: RegisterUserUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(registerUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
