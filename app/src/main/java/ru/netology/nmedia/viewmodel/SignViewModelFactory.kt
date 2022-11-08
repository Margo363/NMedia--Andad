package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.PostRepository


class SignViewModelFactory(
    private val repository: AuthRepository
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(SignUpViewModel::class.java) ->
                SignUpViewModel(repository) as T
            modelClass.isAssignableFrom(SignInViewModel::class.java) ->
                SignInViewModel(repository) as T
            else -> error("Unknown view model class $modelClass")
        }
}