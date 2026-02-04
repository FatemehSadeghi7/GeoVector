package com.example.geovector.presentation.screens.register

import RegisterUserUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geovector.core.date.JalaliConverter
import com.example.geovector.core.date.JalaliDate
import com.example.geovector.core.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val age: String = "",
    val birthDateMillis: Long = 0L,
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

class RegisterViewModel(
    private val registerUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    fun onFullNameChange(v: String) {
        _state.value = _state.value.copy(fullName = v, message = null)
    }

    fun onAgeChange(v: String) {
        val cleaned = v.filter { it.isDigit() }
        _state.value = _state.value.copy(age = cleaned, message = null)
    }

    fun onBirthDateChange(millis: Long) {
        _state.value = _state.value.copy(birthDateMillis = millis, message = null)
    }

    fun onUsernameChange(v: String) {
        _state.value = _state.value.copy(username = v, message = null)
    }

    fun onPasswordChange(v: String) {
        _state.value = _state.value.copy(password = v, message = null)
    }

    fun onBirthDateJalaliSelected(j: JalaliDate) {
        val millis = JalaliConverter.jalaliToEpochMillis(j)
        val computedAge = JalaliConverter.computeAgeFromEpochMillis(millis)

        _state.value = _state.value.copy(
            birthDateMillis = millis,
            age = computedAge.toString(),
            message = null
        )

    }


    fun submit(onSuccess: () -> Unit) {
        val s = _state.value

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, message = null)
            val ageInt = s.age.toIntOrNull() ?: -1
            if (s.birthDateMillis <= 0L) {
                _state.value = s.copy(isLoading = false, message = "تاریخ تولد را انتخاب کنید.")
                return@launch
            }
            val computedAge = JalaliConverter.computeAgeFromEpochMillis(s.birthDateMillis)
            if (ageInt != computedAge) {
                _state.value = s.copy(
                    isLoading = false,
                    message = "سن وارد شده با تاریخ تولد همخوانی ندارد. سن صحیح: $computedAge"
                )
                return@launch
            }


            when (val res = registerUseCase(
                fullName = s.fullName,
                age = ageInt,
                birthDateMillis = s.birthDateMillis,
                username = s.username,
                password = s.password
            )) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = "ثبت‌نام با موفقیت انجام شد."
                    )
                    onSuccess()
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
