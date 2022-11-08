package ru.netology.nmedia.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl


class RefreshPostsWorker(
    appContext: Context,
    params: WorkerParameters,
    private val repository: PostRepository
) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val name = "ru.netology.work.RefreshPostsWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {

        try {
            repository.getAll()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

class RefreshPostsWorkerFactory(private val repository: PostRepository) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        RefreshPostsWorker::class.java.name -> RefreshPostsWorker(
            appContext,
            workerParameters,
            repository
        )
        else -> null
    }

}

