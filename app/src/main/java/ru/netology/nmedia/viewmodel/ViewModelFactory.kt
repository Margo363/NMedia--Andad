package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.repository.PostRepository


class ViewModelFactory(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
    private val workManager: WorkManager
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(PostViewModel::class.java) ->
                PostViewModel(repository, appAuth, workManager) as T
            modelClass.isAssignableFrom(AppAuth::class.java) ->
                AuthViewModel(appAuth) as T
            else -> error("Unknown view model class $modelClass")
        }
}
