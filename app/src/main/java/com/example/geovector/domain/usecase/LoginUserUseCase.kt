import com.example.geovector.domain.repository.AuthRepository

class LoginUserUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(username: String, password: String) =
        repo.login(username, password)
}
