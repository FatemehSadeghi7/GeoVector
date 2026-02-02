import com.example.geovector.domain.repository.AuthRepository

class RegisterUserUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(fullName: String, age: Int, birthDateMillis: Long, username: String, password: String) =
        repo.register(fullName, age, birthDateMillis, username, password)
}
