package com.griot.core.data.song

import com.griot.core.data.database.dao.WordCorrectionDao
import com.griot.core.data.database.entity.WordCorrectionEntity
import com.griot.core.data.model.SongLanguage
import java.time.Clock
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [WordCorrectionRepository], on the Room database. */
@Singleton
internal class WordCorrectionRepositoryImpl @Inject constructor(
    private val dao: WordCorrectionDao,
    private val clock: Clock,
) : WordCorrectionRepository {
    override fun observeCorrections(language: SongLanguage): Flow<Map<String, Int>> =
        dao.observeByLanguage(language).map { corrections -> corrections.associate { it.word to it.syllables } }

    override suspend fun correct(language: SongLanguage, word: String, syllables: Int) {
        dao.upsert(
            WordCorrectionEntity(
                language = language,
                word = word.lowercase(Locale.ROOT),
                syllables = syllables,
                updatedAt = Instant.now(clock),
            ),
        )
    }
}
