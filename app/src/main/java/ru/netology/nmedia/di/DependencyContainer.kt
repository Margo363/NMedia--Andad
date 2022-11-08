package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.viewmodel.ViewModelFactory
import ru.netology.nmedia.work.WorkerFactoryDelegate


class DependencyContainer private constructor(context: Context) {

    companion object {
        private var instance: DependencyContainer? = null
        fun initApp(context: Context) {
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer = requireNotNull(instance)

        private const val BASE_URL = "http://10.0.2.2:9999/api/"
    }

    private val appDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    val postDao = appDb.postDao()
    val postWorkDao = appDb.postWorkDao() // TODO прокатит так с ворк менеджером?

    val appAuth = AppAuth(context)

    private val logging = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okhttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            appAuth.authStateFlow.value.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()

    val apiService: ApiService = retrofit.create()

    val repositoryImpl: PostRepository = PostRepositoryImpl(postDao, postWorkDao, apiService)

    val repositoryAuth = AuthRepository(apiService)

    val workManager = run {
        WorkManager.initialize(
            context, Configuration.Builder()
                .setWorkerFactory(WorkerFactoryDelegate(repositoryImpl))
                .build()
        )

        WorkManager.getInstance(context)
    }

//    val viewModel = ViewModelFactory(
//        repositoryImpl,
//        appAuth,
//        workManager
//    )
}