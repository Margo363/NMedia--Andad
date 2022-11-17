package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.Converters
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dao.PostWorkDao
import ru.netology.nmedia.entity.DraftEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.PostWorkEntity


@Database(
    entities = [PostEntity::class, DraftEntity::class, PostWorkEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postWorkDao(): PostWorkDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}
