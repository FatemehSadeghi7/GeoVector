package com.example.geovector.domain.model

data class User(
    val id: Long,
    val fullName: String,
    val age: Int,
    val birthDateMillis: Long,
    val username: String
)
