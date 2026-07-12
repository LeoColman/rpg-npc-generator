package me.kerooker.rpgnpcgenerator.repository.image

import me.kerooker.rpgnpcgenerator.data.Npc

/**
 * Builds a keyword-dense SD 1.5 prompt straight from the NPC's structured attributes. The
 * randomizer already yields tag-like values (race, profession, alignment...), so no LLM expansion
 * is needed on-device; an optional server-side model can enrich this later.
 */
object PortraitPrompt {

    // Trimmed to the essential look — clothing/SFW words live in SFW_PREFIX (front) + CHILD_SAFETY, and
    // redundant boosters (painterly/artstation/safe for work) were dropped to free CLIP token budget
    // for the descriptors and notes, so less gets truncated at 77 tokens.
    private const val STYLE = "fantasy character portrait, head and shoulders, detailed face, " +
        "dramatic lighting, dungeons and dragons, digital painting, highly detailed"

    // Negative prompt hardened against nudity/sexualization. NOTE: this only has any effect when
    // guidance_scale > 1 (at cfg=1.0 the negative pass is skipped entirely) — see PortraitQueueClient.
    private const val NEGATIVE = "lowres, bad anatomy, bad hands, extra fingers, extra limbs, " +
        "deformed, disfigured, mutation, text, watermark, signature, blurry, cropped, " +
        "nsfw, nude, nudity, naked, topless, bare chest, exposed breasts, cleavage, underwear, " +
        "lingerie, bikini, swimsuit, sexualized, suggestive, provocative, erotic, revealing clothing"

    // Extra guardrails for child NPCs: reinforce a wholesome, fully-clothed portrait on top of the
    // base SFW style so a child portrait can never drift toward anything inappropriate.
    private const val CHILD_SAFETY = "wholesome, innocent, fully clothed, child-appropriate, modest"

    private val CHILD_AGE_KEYWORDS = listOf("child", "criança", "crianca", "niño", "nino", "niña", "nina")

    // Redundant with STYLE's "fully clothed, safe for work", but placed at the FRONT of the prompt so
    // the safety intent survives CLIP's 77-token truncation when user notes push the tail out.
    private const val SFW_PREFIX = "safe for work, fully clothed"
    private const val MAX_TRAITS = 3
    private const val MAX_NOTE_CHARS = 200

    /**
     * SD 1.5 (even fantasy finetunes) barely knows D&D race names, and the low-CFG LCM path follows
     * uncommon tokens weakly, so every race otherwise collapses to a plain human face. We append
     * concrete anatomical anchors the model *does* understand. Keyed by substring so it matches the
     * English, Portuguese and Spanish localized race strings and free-typed edits; order matters — more
     * specific first (drow before elf, half-orc/semiorco before orc, half-elf/semielfo before elf).
     */
    private val RACE_ANATOMY: List<Pair<List<String>, String>> = listOf(
        listOf("dragonborn", "draconato", "dracónido", "draconido") to
            "draconic reptilian humanoid, scaled dragon snout, colored scales covering the face, " +
            "no human skin, horned frill",
        listOf("tiefling", "tiflin") to
            "curved ram horns, deep red skin, glowing solid-color eyes, pointed ears, long tail",
        listOf("drow", "elfo negro", "elfo oscuro") to
            "dark elf, obsidian black-grey skin, stark white hair, long pointed ears, angular features",
        listOf("half-orc", "meio-orc", "meio orc", "semiorco", "medio orco") to
            "grey-green skin, protruding lower tusks, heavy jutting brow, broad muscular build",
        listOf("half-elf", "meio-elfo", "meio elfo", "semielfo", "medio elfo") to
            "subtly pointed ears, refined half-human half-elf features",
        listOf("dwarf", "anão", "anao", "enano") to
            "short and stocky, broad build, thick braided beard, weathered face",
        listOf("halfling", "halfing", "mediano") to
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
            // Safety anchor first, so it survives CLIP's 77-token truncation even when notes are long.
            add(SFW_PREFIX)
            add(npc.age)
            add(npc.gender)
            add(raceWithAnatomy(npc.race))
            add(npc.profession)
            if (npc.alignment.isNotBlank()) add("${npc.alignment} alignment")
            npc.personalityTraits.filter { it.isNotBlank() }.take(MAX_TRAITS).forEach { add(it) }
            // Free-text notes (saved NPCs only) — often hold appearance details; excerpted to stay in budget.
            add(noteExcerpt(npc.notes))
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

    /**
     * A short, single-line excerpt of the user's free-text notes (empty for unsaved NPCs). Newlines
     * are flattened and the text is hard-capped so multi-paragraph lore can't blow the token budget.
     */
    private fun noteExcerpt(notes: String): String =
        notes.trim().replace('\n', ' ').take(MAX_NOTE_CHARS).trim()

    /** Appends concrete physical features for the matched fantasy race; humans/unknowns pass through. */
    private fun raceWithAnatomy(race: String): String {
        if (race.isBlank()) return race
        val key = race.lowercase()
        val anatomy = RACE_ANATOMY.firstOrNull { (keywords, _) -> keywords.any { it in key } }?.second
        return if (anatomy == null) race else "$race, $anatomy"
    }
}
