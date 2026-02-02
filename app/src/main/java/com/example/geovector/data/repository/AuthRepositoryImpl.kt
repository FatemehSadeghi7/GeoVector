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

    override suspend fun register(fullName: String, email: String, password: String): AppResult<User> {
        val name = fullName.trim()
        val mail = email.trim().lowercase()

        if (!Validators.isNameValid(name)) return AppResult.Error("نام معتبر نیست.")
        if (!Validators.isEmailValid(mail)) return AppResult.Error("ایمیل معتبر نیست.")
        if (!Validators.isPasswordValid(password)) return AppResult.Error("پسورد باید حداقل ۶ کاراکتر باشد.")

        val existing = userDao.findByEmail(mail)
        if (existing != null) return AppResult.Error("این ایمیل قبلاً ثبت شده است.")

        val hash = PasswordHasher.sha256(password)
        return try {
            val id = userDao.insert(UserEntity(fullName = name, email = mail, passwordHash = hash))
            AppResult.Success(User(id = id, fullName = name, email = mail))
        } catch (e: Exception) {
            AppResult.Error("خطا در ثبت‌نام. دوباره تلاش کنید.")
        }
    }

    override suspend fun login(email: String, password: String): AppResult<User> {
        val mail = email.trim().lowercase()
        if (!Validators.isEmailValid(mail)) return AppResult.Error("ایمیل معتبر نیست.")
        if (password.isBlank()) return AppResult.Error("پسورد را وارد کنید.")

        val hash = PasswordHasher.sha256(password)
        val user = userDao.findByEmailAndPasswordHash(mail, hash)
            ?: return AppResult.Error("ایمیل یا پسورد اشتباه است.")

        return AppResult.Success(User(id = user.id, fullName = user.fullName, email = user.email))
    }
}
