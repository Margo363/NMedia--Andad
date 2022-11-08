package ru.netology.nmedia.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import ru.netology.nmedia.entity.DraftEntity
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostWorkEntity

@Dao
interface PostWorkDao {

    @Query("SELECT * From PostWorkEntity WHERE id = :id")
    suspend fun getById(id: Long) : PostWorkEntity


    @Insert(onConflict = REPLACE)
    suspend fun insert(work: PostWorkEntity): Long

    @Query("DELETE FROM PostWorkEntity WHERE id = :id")
    suspend fun removeById(id: Long)

}
