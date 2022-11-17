package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nmedia.dto.Post
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Media
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.User

interface ApiService {
    @POST("users/push-tokens")
    suspend fun save(@Body pushToken: PushToken): Response<Unit>

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @POST("posts/{id}/likes")
    suspend fun likedById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authUser(
        @Field("login") login: String?,
        @Field("pass") password: String?
    ): Response<User>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registrationUser(
        @Field("login") login: String?,
        @Field("pass") password: String?,
        @Field("name") name: String
    ): Response<User>
}
