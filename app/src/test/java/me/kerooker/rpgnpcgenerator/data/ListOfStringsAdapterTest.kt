package me.kerooker.rpgnpcgenerator.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ListOfStringsAdapterTest : FunSpec({

    test("round-trips arbitrary user strings (commas, quotes, emoji, newlines, blanks)") {
        val cases = listOf(
            emptyList(),
            listOf("Common"),
            listOf("a,b", "quote\"here", "emoji 🐉", "", "line\nbreak", "back\\slash")
        )

        cases.forEach { original ->
            val encoded = ListOfStringsAdapter.encode(original)
            ListOfStringsAdapter.decode(encoded) shouldBe original
        }
    }

    test("decodes an empty string to an empty list") {
        ListOfStringsAdapter.decode("") shouldBe emptyList()
    }
})
