package com.example.geovector.presentation.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geovector.core.result.AppResult
import com.example.geovector.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val loginUseCase = AppModule.provideLoginUseCase(app)

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChange(v: String) = _state.value.let { _state.value = it.copy(email = v, message = null) }
    fun onPasswordChange(v: String) = _state.value.let { _state.value = it.copy(password = v, message = null) }

    fun submit() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, message = null)
            when (val res = loginUseCase(s.email, s.password)) {
                is AppResult.Success -> _state.value =
                    _state.value.copy(isLoading = false, message = "ورود موفق: ${res.data.fullName}")
                is AppResult.Error -> _state.value =
                    _state.value.copy(isLoading = false, message = res.message)
            }
        }
    }
}
