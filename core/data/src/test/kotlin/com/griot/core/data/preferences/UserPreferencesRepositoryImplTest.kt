package com.griot.core.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.griot.core.data.model.SongLanguage
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class UserPreferencesRepositoryImplTest {
    @TempDir
    lateinit var folder: File

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: UserPreferencesRepositoryImpl

    @BeforeEach
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { File(folder, "test.preferences_pb") }
        repository = UserPreferencesRepositoryImpl(dataStore)
    }

    @AfterEach
    fun tearDown() {
        scope.cancel()
    }

    @Nested
    inner class OnboardingCompleted {
        @Test
        fun `should not be completed on a new install`() = runTest {
            // when
            val completed = repository.onboardingCompleted.first()

            // then
            completed.shouldBeFalse()
        }

        @Test
        fun `should be completed once recorded`() = runTest {
            // when
            repository.setOnboardingCompleted()

            // then
            repository.onboardingCompleted.first().shouldBeTrue()
        }
    }

    @Nested
    inner class AuthorName {
        @Test
        fun `should have no name on a new install`() = runTest {
            // when
            val name = repository.authorName.first()

            // then
            name.shouldBeNull()
        }

        @Test
        fun `should keep the name saved`() = runTest {
            // when
            repository.setAuthorName("my-name")

            // then
            repository.authorName.first() shouldBe "my-name"
        }
    }

    @Nested
    inner class DefaultSongLanguage {
        @Test
        fun `should write new songs in French on a new install of a French phone`() = runTest {
            // given
            val locale = Locale.getDefault()
            Locale.setDefault(Locale.FRANCE)

            // when
            val language = try {
                repository.defaultSongLanguage.first()
            } finally {
                Locale.setDefault(locale)
            }

            // then
            language shouldBe SongLanguage.FRENCH
        }

        @Test
        fun `should write new songs in English on a new install of a phone in another language`() = runTest {
            // given
            val locale = Locale.getDefault()
            Locale.setDefault(Locale.US)

            // when
            val language = try {
                repository.defaultSongLanguage.first()
            } finally {
                Locale.setDefault(locale)
            }

            // then
            language shouldBe SongLanguage.ENGLISH
        }

        @Test
        fun `should keep the language saved`() = runTest {
            // when
            repository.setDefaultSongLanguage(SongLanguage.ENGLISH)

            // then
            repository.defaultSongLanguage.first() shouldBe SongLanguage.ENGLISH
        }
    }

    @Nested
    inner class EditorFontSize {
        @Test
        fun `should show the lyrics at 18 sp on a new install`() = runTest {
            // when
            val size = repository.editorFontSize.first()

            // then
            size shouldBe 18
        }

        @Test
        fun `should keep the size saved`() = runTest {
            // when
            repository.setEditorFontSize(24)

            // then
            repository.editorFontSize.first() shouldBe 24
        }
    }

    @Nested
    inner class ShowRhymes {
        @Test
        fun `should colour the rhymes on a new install`() = runTest {
            // when
            val show = repository.showRhymes.first()

            // then
            show.shouldBeTrue()
        }

        @Test
        fun `should keep the rhymes hidden once hidden`() = runTest {
            // when
            repository.setShowRhymes(false)

            // then
            repository.showRhymes.first().shouldBeFalse()
        }
    }

    @Nested
    inner class SetSynced {
        @Test
        fun `should have no sync on a new install`() = runTest {
            // when
            val lastSyncedAt = repository.lastSyncedAt.first()

            // then
            lastSyncedAt.shouldBeNull()
            repository.syncCursor.first().shouldBeNull()
        }

        @Test
        fun `should keep the time and the cursor of the last sync`() = runTest {
            // when
            repository.setSynced(at = Instant.parse("2026-11-17T01:00:00Z"), cursor = "my-cursor")

            // then
            repository.lastSyncedAt.first() shouldBe Instant.parse("2026-11-17T01:00:00Z")
            repository.syncCursor.first() shouldBe "my-cursor"
        }

        @Test
        fun `should forget the cursor when the remote store gives none`() = runTest {
            // given
            repository.setSynced(at = Instant.parse("2026-11-17T01:00:00Z"), cursor = "my-cursor")

            // when
            repository.setSynced(at = Instant.parse("2026-11-17T02:00:00Z"), cursor = null)

            // then
            repository.syncCursor.first().shouldBeNull()
        }
    }
}
