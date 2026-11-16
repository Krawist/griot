package com.griot.core.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.griot.core.data.model.SongLanguage
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
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
        fun `should write new songs in the automatic language on a new install`() = runTest {
            // when
            val language = repository.defaultSongLanguage.first()

            // then
            language shouldBe SongLanguage.AUTOMATIC
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
}
