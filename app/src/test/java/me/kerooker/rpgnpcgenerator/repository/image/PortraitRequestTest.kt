package me.kerooker.rpgnpcgenerator.repository.image

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PortraitRequestTest : FunSpec({

    test("default width and height match the portrait card aspect (512x640)") {
        val request = PortraitRequest(prompt = "a prompt", negativePrompt = "a negative prompt")

        request.width shouldBe 512
        request.height shouldBe 640
    }

    test("all fields round-trip through the constructor") {
        val request = PortraitRequest(
            prompt = "a prompt",
            negativePrompt = "a negative prompt",
            width = 256,
            height = 320
        )

        request.prompt shouldBe "a prompt"
        request.negativePrompt shouldBe "a negative prompt"
        request.width shouldBe 256
        request.height shouldBe 320
    }

    test("data class equality holds for identical field values") {
        val a = PortraitRequest(prompt = "p", negativePrompt = "n")
        val b = PortraitRequest(prompt = "p", negativePrompt = "n")

        a shouldBe b
    }

    test("copy overrides only the specified field, keeping the rest including defaults") {
        val original = PortraitRequest(prompt = "p", negativePrompt = "n")

        val copy = original.copy(width = 1024)

        copy.prompt shouldBe "p"
        copy.negativePrompt shouldBe "n"
        copy.width shouldBe 1024
        copy.height shouldBe 640
    }
})
