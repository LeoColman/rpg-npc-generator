package me.kerooker.rpgnpcgenerator.repository.image

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe

class RemoteImageConfigTest : FunSpec({

    test("enabled is true only when baseUrl and password are both non-blank") {
        val cases = listOf(
            Triple("https://npc-fast.colman.com.br", "secret", true),
            Triple("", "secret", false),
            Triple("https://npc-fast.colman.com.br", "", false),
            Triple("", "", false),
            Triple("   ", "secret", false),
            Triple("https://npc-fast.colman.com.br", "   ", false),
            Triple("   ", "   ", false)
        )

        cases.forAll { (baseUrl, password, expected) ->
            RemoteImageConfig(baseUrl = baseUrl, username = "user", password = password).enabled shouldBe expected
        }
    }

    test("username has no effect on enabled") {
        RemoteImageConfig(baseUrl = "https://npc-fast.colman.com.br", username = "", password = "secret")
            .enabled shouldBe true

        RemoteImageConfig(baseUrl = "https://npc-fast.colman.com.br", username = "   ", password = "secret")
            .enabled shouldBe true
    }

    test("fields are exposed unchanged") {
        val config = RemoteImageConfig(
            baseUrl = "https://npc-fast.colman.com.br",
            username = "kerooker",
            password = "secret"
        )

        config.baseUrl shouldBe "https://npc-fast.colman.com.br"
        config.username shouldBe "kerooker"
        config.password shouldBe "secret"
    }
})
