package com.example.geovector.presentation.screens.login

import LoginUserUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geovector.core.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

class LoginViewModel(
    private val loginUseCase: LoginUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onUsernameChange(v: String) {
        _state.value = _state.value.copy(username = v, message = null)
    }

    fun onPasswordChange(v: String) {
        _state.value = _state.value.copy(password = v, message = null)
    }

    fun submit(onSuccess: (() -> Unit)? = null) {
        val s = _state.value

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, message = null)

            when (val res = loginUseCase(username = s.username, password = s.password)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "ورود موفقیت‌آمیز بود."
                    )
                    onSuccess?.invoke()
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = res.message
                    )
                }
            }
        }
    }
}
