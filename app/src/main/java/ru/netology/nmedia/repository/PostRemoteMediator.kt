package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
//                    реализация с вебинара
//                    apiService.getLatest(state.config.initialLoadSize)

                    postRemoteKeyDao.max()?.let {
                        apiService.getAfter(it, state.config.pageSize)
                    } ?: apiService.getLatest(state.config.initialLoadSize)

                }
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)

//                    реализация с вебинара
//                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
//                    apiService.getAfter(id, state.config.pageSize)
                }

            }

            if (!result.isSuccessful) throw HttpException(result)
            val data = result.body() ?: throw HttpException(result)

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                data.first().id
                            )
                        )
                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    data.last().id
                                )
                            )
                        }

//                    реализация с вебинара
//                        postRemoteKeyDao.insert(
//                            listOf(
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.AFTER,
//                                    data.first().id
//                                ),
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.BEFORE,
//                                    data.last().id
//                                )
//                            )
//                        )
//                      postDao.clear()
                    }
                    LoadType.PREPEND -> {
//                         в новой реализации ничего не делаем
//
//                        РЕАЛИЗАЦИЯ НА ВЕБИНАРЕ
//                        postRemoteKeyDao.insert(
//                            PostRemoteKeyEntity(
//                                PostRemoteKeyEntity.KeyType.AFTER,
//                                data.first().id
//                            )
//                        )
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                data.last().id
                            )
                        )
                    }
                }

                postDao.insert(data.map { PostEntity.fromDto(it) })
            }

            return MediatorResult.Success(data.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}