package me.kerooker.rpgnpcgenerator.repository.image

import me.kerooker.rpgnpcgenerator.data.Npc

/**
 * Builds a keyword-dense SD 1.5 prompt straight from the NPC's structured attributes. The
 * randomizer already yields tag-like values (race, profession, alignment...), so no LLM expansion
 * is needed on-device; an optional server-side model can enrich this later.
 */
object PortraitPrompt {

    private const val STYLE = "fantasy character portrait, head and shoulders, fully clothed, " +
        "wearing clothing, modest, detailed face, dramatic lighting, painterly, dungeons and dragons, " +
        "digital painting, artstation, highly detailed, safe for work"

    // Negative prompt hardened against nudity/sexualization. NOTE: this only has any effect when
    // guidance_scale > 1 (at cfg=1.0 the negative pass is skipped entirely) — see PortraitQueueClient.
    private const val NEGATIVE = "lowres, bad anatomy, bad hands, extra fingers, extra limbs, " +
        "deformed, disfigured, mutation, text, watermark, signature, blurry, cropped, " +
        "nsfw, nude, nudity, naked, topless, bare chest, exposed breasts, cleavage, underwear, " +
        "lingerie, bikini, swimsuit, sexualized, suggestive, provocative, erotic, revealing clothing"

    // Extra guardrails for child NPCs: reinforce a wholesome, fully-clothed portrait on top of the
    // base SFW style so a child portrait can never drift toward anything inappropriate.
    private const val CHILD_SAFETY = "wholesome, innocent, fully clothed, child-appropriate, modest"

    private val CHILD_AGE_KEYWORDS = listOf("child", "criança", "crianca")

    /**
     * SD 1.5 (even fantasy finetunes) barely knows D&D race names, and the low-CFG LCM path follows
     * uncommon tokens weakly, so every race otherwise collapses to a plain human face. We append
     * concrete anatomical anchors the model *does* understand. Keyed by substring so it matches both
     * the English and Portuguese localized race strings and free-typed edits; order matters — more
     * specific first (drow before elf, half-orc before orc, half-elf before elf).
     */
    private val RACE_ANATOMY: List<Pair<List<String>, String>> = listOf(
        listOf("dragonborn", "draconato") to
            "draconic reptilian humanoid, scaled dragon snout, colored scales covering the face, " +
            "no human skin, horned frill",
        listOf("tiefling") to
            "curved ram horns, deep red skin, glowing solid-color eyes, pointed ears, long tail",
        listOf("drow", "elfo negro") to
            "dark elf, obsidian black-grey skin, stark white hair, long pointed ears, angular features",
        listOf("half-orc", "meio-orc", "meio orc") to
            "grey-green skin, protruding lower tusks, heavy jutting brow, broad muscular build",
        listOf("half-elf", "meio-elfo", "meio elfo") to
            "subtly pointed ears, refined half-human half-elf features",
        listOf("dwarf", "anão", "anao") to
            "short and stocky, broad build, thick braided beard, weathered face",
        listOf("halfling", "halfing") to
            "small childlike stature, round friendly face, curly hair",
        listOf("gnome", "gnomo") to
            "very small stature, oversized curious eyes, pointed ears, wild hair",
        listOf("elf", "elfo") to
            "long pointed ears, slender angular ethereal features",
        listOf("orc", "orco") to
            "green skin, protruding tusks, heavy brow, savage muscular build"
    )

    fun forNpc(npc: Npc): PortraitRequest {
        val descriptors = buildList {
            add(npc.age)
            add(npc.gender)
            add(raceWithAnatomy(npc.race))
            add(npc.profession)
            if (npc.alignment.isNotBlank()) add("${npc.alignment} alignment")
            npc.personalityTraits.filter { it.isNotBlank() }.take(2).forEach { add(it) }
        }.filter { it.isNotBlank() }.joinToString(", ")

        val style = if (isChild(npc.age)) "$STYLE, $CHILD_SAFETY" else STYLE
        return PortraitRequest(
            prompt = "$descriptors, $style",
            negativePrompt = NEGATIVE
        )
    }

    /** Child NPCs get the extra [CHILD_SAFETY] clause; matches the localized age string (en + pt). */
    private fun isChild(age: String): Boolean {
        val normalized = age.lowercase()
        return CHILD_AGE_KEYWORDS.any { it in normalized }
    }

    /** Appends concrete physical features for the matched fantasy race; humans/unknowns pass through. */
    private fun raceWithAnatomy(race: String): String {
        if (race.isBlank()) return race
        val key = race.lowercase()
        val anatomy = RACE_ANATOMY.firstOrNull { (keywords, _) -> keywords.any { it in key } }?.second
        return if (anatomy == null) race else "$race, $anatomy"
    }
}
