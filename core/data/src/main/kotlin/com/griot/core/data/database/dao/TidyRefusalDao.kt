package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.griot.core.data.database.entity.TidyRefusalEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TidyRefusalDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(refusals: List<TidyRefusalEntity>)

    /** The ids (`kind:key`) of every refused proposal. */
    @Query("SELECT id FROM tidy_refusals")
    fun observeIds(): Flow<List<String>>
}
