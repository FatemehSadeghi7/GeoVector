import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geovector.domain.usecase.RegisterUserUseCase
import com.example.geovector.presentation.screens.register.RegisterViewModel

class RegisterViewModelFactory(
    private val registerUseCase: RegisterUserUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(registerUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
