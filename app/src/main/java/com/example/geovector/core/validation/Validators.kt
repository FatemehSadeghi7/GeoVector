package com.example.geovector.core.validation

object Validators {
    fun isEmailValid(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isPasswordValid(password: String): Boolean = password.length >= 6

    fun isNameValid(name: String): Boolean = name.trim().length >= 2
}
