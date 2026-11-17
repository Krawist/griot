package com.griot.core.ai

import com.griot.core.phonetics.PhoneticLanguage
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class RemarkCheckTest {
    @Nested
    inner class KeepRemarks {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "Le rythme est plat.",
                "« cogne sur la tôle » est une image forte.",
                "Le mot \"tôle\" sonne bien.",
                "« La poussière danse » ouvre bien la section.",
            ],
        )
        fun `should keep a remark that quotes only the author`(remark: String) {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "verse 1",
                verse = "Le soleil cogne sur la tôle",
                before = listOf("La poussière danse sur nos vies."),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val remarks = keepRemarks(remarks = listOf(remark), request = request)

            // then
            remarks shouldContainExactly listOf(remark)
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "Essaie plutôt « Le soleil frappe sur la tôle ».",
                "Tu pourrais écrire \"Le soleil brûle la tôle\".",
                "Mieux : “le soleil tape fort”.",
            ],
        )
        fun `should drop a remark that holds a rewritten verse`(remark: String) {
            // given
            val request = VerseRequest(
                language = PhoneticLanguage.FRENCH,
                section = "verse 1",
                verse = "Le soleil cogne sur la tôle",
                before = listOf("La poussière danse sur nos vies."),
                after = emptyList(),
                targetSyllables = 8,
            )

            // when
            val remarks = keepRemarks(remarks = listOf(remark), request = request)

            // then
            remarks.shouldBeEmpty()
        }
    }
}
