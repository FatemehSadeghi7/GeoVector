package com.example.geovector.core.validation

object Validators {
    fun isNameValid(name: String) = name.trim().length >= 2
    fun isUsernameValid(username: String) = username.trim().length >= 3
    fun isPasswordValid(password: String) = password.length >= 6
    fun isAgeValid(age: Int) = age in 1..120
    fun isBirthDateValid(birthDateMillis: Long) = birthDateMillis > 0L
}
