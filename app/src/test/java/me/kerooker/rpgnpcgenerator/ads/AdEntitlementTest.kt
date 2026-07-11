package me.kerooker.rpgnpcgenerator.ads

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AdEntitlementTest : FunSpec({

    test("ads are enabled by default (deadline 0)") {
        AdEntitlement.adsEnabled(now = 1_000L, adFreeUntil = 0L) shouldBe true
    }

    test("ads are hidden strictly before the deadline and shown at/after it") {
        AdEntitlement.adsEnabled(now = 99L, adFreeUntil = 100L) shouldBe false
        AdEntitlement.adsEnabled(now = 100L, adFreeUntil = 100L) shouldBe true
        AdEntitlement.adsEnabled(now = 101L, adFreeUntil = 100L) shouldBe true
    }

    test("granting from an expired deadline starts the week at now") {
        AdEntitlement.grantedUntil(now = 1_000L, currentUntil = 500L, weekMillis = 7L) shouldBe 1_007L
    }

    test("granting while still ad-free extends (stacks) from the existing deadline") {
        AdEntitlement.grantedUntil(now = 1_000L, currentUntil = 5_000L, weekMillis = 7L) shouldBe 5_007L
    }
})
