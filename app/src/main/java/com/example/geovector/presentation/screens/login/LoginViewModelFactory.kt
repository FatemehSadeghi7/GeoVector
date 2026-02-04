package com.example.geovector.presentation.screens.login

import LoginUserUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geovector.data.local.dao.UserDao

class LoginViewModelFactory(
    private val loginUseCase: LoginUserUseCase,
    private val userDao: UserDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(loginUseCase,userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
