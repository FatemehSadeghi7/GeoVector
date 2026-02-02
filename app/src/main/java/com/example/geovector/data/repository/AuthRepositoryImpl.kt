package com.example.geovector.data.repository

import com.example.geovector.core.result.AppResult
import com.example.geovector.core.security.PasswordHasher
import com.example.geovector.core.validation.Validators
import com.example.geovector.data.local.dao.UserDao
import com.example.geovector.data.local.entity.UserEntity
import com.example.geovector.domain.model.User
import com.example.geovector.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        age: Int,
        birthDateMillis: Long,
        username: String,
        password: String
    ): AppResult<User> {

        val name = fullName.trim()
        val user = username.trim().lowercase()

        if (!Validators.isNameValid(name)) return AppResult.Error("نام معتبر نیست.")
        if (!Validators.isAgeValid(age)) return AppResult.Error("سن معتبر نیست.")
        if (!Validators.isBirthDateValid(birthDateMillis)) return AppResult.Error("تاریخ تولد را انتخاب کنید.")
        if (!Validators.isUsernameValid(user)) return AppResult.Error("نام کاربری باید حداقل ۳ کاراکتر باشد.")
        if (!Validators.isPasswordValid(password)) return AppResult.Error("رمز عبور باید حداقل ۶ کاراکتر باشد.")

        if (userDao.findByUsername(user) != null) return AppResult.Error("این نام کاربری قبلاً ثبت شده است.")

        val hash = PasswordHasher.sha256(password)

        return try {
            val id = userDao.insert(
                UserEntity(
                    fullName = name,
                    age = age,
                    birthDateMillis = birthDateMillis,
                    username = user,
                    passwordHash = hash
                )
            )
            AppResult.Success(User(id, name, age, birthDateMillis, user))
        } catch (e: Exception) {
            AppResult.Error("خطا در ثبت‌نام. دوباره تلاش کنید.")
        }
    }

    override suspend fun login(username: String, password: String): AppResult<User> {
        val user = username.trim().lowercase()

        if (!Validators.isUsernameValid(user)) return AppResult.Error("نام کاربری معتبر نیست.")
        if (password.isBlank()) return AppResult.Error("رمز عبور را وارد کنید.")

        val hash = PasswordHasher.sha256(password)
        val entity = userDao.loginByUsernamePassword(user, hash)
            ?: return AppResult.Error("نام کاربری یا رمز عبور اشتباه است.")

        return AppResult.Success(
            User(
                id = entity.id,
                fullName = entity.fullName,
                age = entity.age,
                birthDateMillis = entity.birthDateMillis,
                username = entity.username
            )
        )
    }

}
