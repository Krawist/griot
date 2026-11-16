package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.griot.core.data.database.entity.WordCorrectionEntity
import com.griot.core.data.model.SongLanguage
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WordCorrectionDao {
    /** Saves the correction of a word, replacing the one before (no other table points to this one). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(correction: WordCorrectionEntity)

    /** The corrections of the words of [language]. */
    @Query("SELECT * FROM word_corrections WHERE language = :language")
    fun observeByLanguage(language: SongLanguage): Flow<List<WordCorrectionEntity>>
}
