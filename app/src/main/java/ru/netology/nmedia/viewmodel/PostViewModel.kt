package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.RetryType
import ru.netology.nmedia.module.FeedModelState
import ru.netology.nmedia.module.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import ru.netology.nmedia.work.RemovePostWorker
import ru.netology.nmedia.work.SavePostWorker
import java.io.File
import javax.inject.Inject

private val emptyPost = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    authorId = 0,
    content = "",
    published = "",
    likes = 0,
    likedByMe = false,
    share = false,
    sharesCount = 0
)

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = cached



    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(emptyPost)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> _dataState.postValue(FeedModelState(error = true)) }
//            .asLiveData(Dispatchers.Default)
//    }

    private val noPhoto = PhotoModel()
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun loadNewPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getNewPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    val id = repository.saveWork(
                        it, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    val data = workDataOf(SavePostWorker.postKey to id)
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val request = OneTimeWorkRequestBuilder<SavePostWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    workManager.enqueue(request)

                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }

    }

    fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (it.content != text)
                edited.value = it.copy(content = text)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likedById(id)
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryType.LIKE, retryId = id)
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            repository.unlikeById(id)
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryType.UNLIKE, retryId = id)
        }
    }

    fun shareById(id: Long) = viewModelScope.launch {
        repository.shareById(id)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
//            repository.removeById(id)

            val data = workDataOf(RemovePostWorker.postKey to id)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<RemovePostWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(request)

            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryType.REMOVE, retryId = id)
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }


    fun cancelEditing() = edited.value?.let {
//        repository.cancelEditing(it)
    }


//    fun saveDraft(draft: String?) = repository.saveDraft(draft)
//    fun getDraft(): String? = repository.getDraft()

}
