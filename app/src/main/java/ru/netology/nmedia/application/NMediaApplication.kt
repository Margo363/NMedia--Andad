package ru.netology.nmedia.application

import android.app.Application
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.work.RefreshPostsWorker
import java.util.concurrent.TimeUnit


class NMediaApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        setupAuth()
        setupWork()
//        DependencyContainer.initApp(this)
    }

    private fun setupWork() {
        appScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<RefreshPostsWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this@NMediaApplication).enqueueUniquePeriodicWork(
                RefreshPostsWorker.name,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    private fun setupAuth() {
        appScope.launch {
            DependencyContainer.initApp(this@NMediaApplication)
        }
    }
}