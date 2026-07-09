package me.kerooker.rpgnpcgenerator.repository.image

import me.kerooker.rpgnpcgenerator.data.Npc

/**
 * Builds a keyword-dense SD 1.5 prompt straight from the NPC's structured attributes. The
 * randomizer already yields tag-like values (race, profession, alignment...), so no LLM expansion
 * is needed on-device; an optional server-side model can enrich this later.
 */
object PortraitPrompt {

    private const val STYLE = "fantasy character portrait, head and shoulders, detailed face, " +
        "dramatic lighting, painterly, dungeons and dragons, digital painting, artstation, highly detailed"

    private const val NEGATIVE = "lowres, bad anatomy, bad hands, extra fingers, extra limbs, " +
        "deformed, disfigured, mutation, text, watermark, signature, blurry, cropped, nsfw"

    fun forNpc(npc: Npc): PortraitRequest {
        val descriptors = buildList {
            add(npc.age)
            add(npc.gender)
            add(npc.race)
            add(npc.profession)
            if (npc.alignment.isNotBlank()) add("${npc.alignment} alignment")
            npc.personalityTraits.filter { it.isNotBlank() }.take(2).forEach { add(it) }
        }.filter { it.isNotBlank() }.joinToString(", ")

        return PortraitRequest(
            prompt = "$descriptors, $STYLE",
            negativePrompt = NEGATIVE
        )
    }
}
