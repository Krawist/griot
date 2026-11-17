package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.FavoriteEntity

@Dao
internal interface FavoriteDao {
    @Insert
    suspend fun insert(row: FavoriteEntity)

    @Update
    suspend fun update(row: FavoriteEntity)

    /** Writes the row, replacing the one of the same song. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE songId = :songId")
    suspend fun getById(songId: String): FavoriteEntity?
}
