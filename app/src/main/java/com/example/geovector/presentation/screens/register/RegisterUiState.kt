package com.example.geovector.presentation.screens.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geovector.core.result.AppResult
import com.example.geovector.di.AppModule
import com.example.geovector.domain.usecase.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

class RegisterViewModel(
    private val registerUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    fun onFullNameChange(v: String) = _state.value.let { _state.value = it.copy(fullName = v, message = null) }
    fun onEmailChange(v: String) = _state.value.let { _state.value = it.copy(email = v, message = null) }
    fun onPasswordChange(v: String) = _state.value.let { _state.value = it.copy(password = v, message = null) }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, message = null)
            when (val res = registerUseCase(s.fullName, s.email, s.password)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, message = "ثبت‌نام با موفقیت انجام شد.")
                    onSuccess()
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, message = res.message)
                }
            }
        }
    }
}