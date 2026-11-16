package com.griot.feature.onboarding.importing

import com.griot.core.data.importing.FakeDocumentReader
import com.griot.core.data.importing.FakeTextImporter
import com.griot.core.data.model.ImportResult
import com.griot.core.data.model.ImportedText
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ImportViewModelTest {
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnFilesPicked {
        @Test
        fun `should import the files read and show what was created`() {
            // given
            val file = ImportedText(name = "my-name", text = "my-text")
            val importer = FakeTextImporter(result = ImportResult(songs = 1, sections = 2, failed = emptyList()))
            val viewModel = ImportViewModel(reader = FakeDocumentReader(mapOf("my-uri" to file)), importer = importer)

            // when
            viewModel.onFilesPicked(listOf("my-uri"))

            // then
            viewModel.uiState.value shouldBe ImportUiState(
                importing = false,
                result = ImportResult(songs = 1, sections = 2, failed = emptyList()),
            )
            importer.imported shouldContainExactly listOf(file)
        }

        @Test
        fun `should add up the imports of the screen`() {
            // given
            val importer = FakeTextImporter(result = ImportResult(songs = 1, sections = 2, failed = listOf("my-name")))
            val viewModel = ImportViewModel(reader = FakeDocumentReader(), importer = importer)
            viewModel.onFilesPicked(listOf("my-uri-1"))

            // when
            viewModel.onFilesPicked(listOf("my-uri-2"))

            // then
            viewModel.uiState.value.result shouldBe
                ImportResult(songs = 2, sections = 4, failed = listOf("my-name", "my-name"))
        }

        @Test
        fun `should do nothing when no file was picked`() {
            // given
            val importer = FakeTextImporter()
            val viewModel = ImportViewModel(reader = FakeDocumentReader(), importer = importer)

            // when
            viewModel.onFilesPicked(emptyList())

            // then
            viewModel.uiState.value shouldBe ImportUiState()
            importer.imported.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnPaste {
        @Test
        fun `should import the text as a song not named yet`() {
            // given
            val importer = FakeTextImporter(result = ImportResult(songs = 1, sections = 1, failed = emptyList()))
            val viewModel = ImportViewModel(reader = FakeDocumentReader(), importer = importer)

            // when
            viewModel.onPaste("my-text")

            // then
            importer.imported shouldContainExactly listOf(ImportedText(name = "", text = "my-text"))
        }

        @Test
        fun `should not import a blank text`() {
            // given
            val importer = FakeTextImporter()
            val viewModel = ImportViewModel(reader = FakeDocumentReader(), importer = importer)

            // when
            viewModel.onPaste("  ")

            // then
            importer.imported.shouldBeEmpty()
        }
    }
}
