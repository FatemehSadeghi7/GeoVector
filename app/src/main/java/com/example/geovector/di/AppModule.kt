package com.example.geovector.di

import LoginUserUseCase
import RegisterUserUseCase
import android.content.Context
import androidx.room.Room
import com.example.geovector.data.local.db.AppDatabase
import com.example.geovector.data.repository.AuthRepositoryImpl
import com.example.geovector.domain.repository.AuthRepository


object AppModule {

    @Volatile private var db: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase =
        db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "geovector.db"
            ).build().also { db = it }
        }

    fun provideAuthRepository(context: Context): AuthRepository {
        val dao = provideDatabase(context).userDao()
        return AuthRepositoryImpl(dao)
    }

    fun provideRegisterUseCase(context: Context) =
        RegisterUserUseCase(provideAuthRepository(context))

    fun provideLoginUseCase(context: Context) =
        LoginUserUseCase(provideAuthRepository(context))

    fun provideLocationDao(context: Context) =
        provideDatabase(context).locationDao()

    fun provideUserDao(context: Context) =
        provideDatabase(context).userDao()
}
