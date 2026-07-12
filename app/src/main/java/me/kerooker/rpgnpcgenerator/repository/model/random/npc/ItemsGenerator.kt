@file:Suppress("MagicNumber")

package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import kotlin.random.Random

/**
 * Rolls a small, believable starting inventory for an NPC (2-5 plain-string entries):
 *
 *  1. A coin purse — always present, weighted low because most NPCs are commoners.
 *  2. A profession-influenced tool — only when the profession matches a known trade.
 *  3. A simple weapon — only sometimes, and never anything fancy.
 *  4. Zero to two trinkets/curiosities from a flavour table, topped up so the purse is never alone.
 *
 * Items are plain, table-ready strings.
 *
 * ### Where the item data lives (and why)
 * Unlike names/professions/motivations — which are one-per-locale raw resource *files* read by the
 * [FileGenerator]s — an item roll needs a *keyword→item* association (the profession tool) that a flat
 * list of lines can't express. So the tables live in code with English + Portuguese variants, exactly
 * as the task calls for. The profession table mirrors `PortraitPrompt.RACE_ANATOMY`: substring keywords
 * in **both** languages so it triggers on either localized (or hand-edited) profession string. The
 * output language is chosen by [portuguese], which the DI layer resolves from the active locale once —
 * the same lifetime as a [FileGenerator] loading its locale's raw file.
 */
class ItemsGenerator(
    private val portuguese: Boolean,
    private val random: Random = Random.Default
) {

    /**
     * Rolls a fresh inventory for an NPC of the given (already-localized) [profession]. The result
     * always holds between [MIN_ITEMS] and [MAX_ITEMS] entries, and its first entry is always the
     * coin purse.
     */
    fun generate(profession: String): List<String> {
        val items = mutableListOf<String>()
        items += coinPurse()
        professionItem(profession)?.let { items += it }
        if (chance(WEAPON_CHANCE)) items += weapon()

        // 0-2 flavour trinkets, but topped up so the purse is never the only thing an NPC carries: the
        // floor keeps the total at MIN_ITEMS, and MAX_TRINKETS keeps it at MAX_ITEMS.
        val minTrinkets = (MIN_ITEMS - items.size).coerceAtLeast(0)
        val trinketCount = random.nextInt(minTrinkets, MAX_TRINKETS + 1)
        items += trinkets(trinketCount)
        return items
    }

    /** A single flavour trinket, used when re-rolling or appending one item on the generator screen. */
    fun randomTrinket(): String = pick(if (portuguese) TRINKETS_PT else TRINKETS_EN)

    /** "Coin pouch (7 copper)" — the amount is weighted low: mostly a few copper, rarely gold. */
    private fun coinPurse(): String {
        val (denominations, prefix) = if (portuguese) {
            COIN_DENOMINATIONS_PT to COIN_PURSE_PREFIX_PT
        } else {
            COIN_DENOMINATIONS_EN to COIN_PURSE_PREFIX_EN
        }
        // Weighted toward the cheapest coin (copper) so purses stay commoner-poor.
        val (denomination, maxAmount) = denominations.random(random)
        val amount = random.nextInt(1, maxAmount + 1)
        return "$prefix ($amount $denomination)"
    }

    /**
     * The tool of the NPC's trade, or null when the profession isn't a recognised trade. Matches the
     * [PROFESSION_ITEMS] keyword table (both languages) by substring, most-specific entries first.
     */
    private fun professionItem(profession: String): String? {
        if (profession.isBlank()) return null
        val key = profession.lowercase()
        val match = PROFESSION_ITEMS.firstOrNull { (keywords, _, _) -> keywords.any { it in key } }
            ?: return null
        return if (portuguese) match.pt else match.en
    }

    private fun weapon(): String = pick(if (portuguese) WEAPONS_PT else WEAPONS_EN)

    /** [count] distinct trinkets (never a repeat within one roll). */
    private fun trinkets(count: Int): List<String> =
        (if (portuguese) TRINKETS_PT else TRINKETS_EN).shuffled(random).take(count)

    private fun pick(from: List<String>): String = from.random(random)

    private fun chance(probability: Double): Boolean = random.nextDouble() <= probability

    /** A profession keyword table entry: [keywords] (en + pt substrings) map to a localized item. */
    private data class ProfessionItem(val keywords: List<String>, val en: String, val pt: String)

    companion object {
        const val MIN_ITEMS = 2
        const val MAX_ITEMS = 5
        const val MAX_TRINKETS = 2

        // A weapon shows up on a minority of NPCs — most commoners aren't armed.
        private const val WEAPON_CHANCE = 0.35

        const val COIN_PURSE_PREFIX_EN = "Coin pouch"
        const val COIN_PURSE_PREFIX_PT = "Bolsa de moedas"

        // Denomination paired with its (low) maximum amount. Copper appears most often (it's listed
        // repeatedly) so most purses hold a handful of copper; gold is a rare windfall.
        private val COIN_DENOMINATIONS_EN = listOf(
            "copper" to 20, "copper" to 20, "copper" to 20,
            "silver" to 10, "silver" to 10,
            "gold" to 3
        )
        private val COIN_DENOMINATIONS_PT = listOf(
            "de cobre" to 20, "de cobre" to 20, "de cobre" to 20,
            "de prata" to 10, "de prata" to 10,
            "de ouro" to 3
        )

        // Keyed by substring so it matches both the English and Portuguese localized profession
        // strings (and free-typed edits); order matters — more specific keywords come first.
        private val PROFESSION_ITEMS: List<ProfessionItem> = listOf(
            ProfessionItem(
                listOf("smith", "ferreiro", "ferrador", "armeiro"),
                "A set of smith's tools", "Um conjunto de ferramentas de ferreiro"
            ),
            ProfessionItem(
                listOf("alchemist", "alquimista"),
                "A pouch of alchemical reagents", "Uma bolsa de reagentes alquímicos"
            ),
            ProfessionItem(
                listOf("herbalist", "herb", "herborista", "erva"),
                "A bundle of dried herbs", "Um maço de ervas secas"
            ),
            ProfessionItem(
                listOf("priest", "cleric", "monk", "sacerdote", "padre", "monge", "clérig"),
                "A holy symbol on a cord", "Um símbolo sagrado num cordão"
            ),
            ProfessionItem(
                listOf("cook", "baker", "cozinh", "padeiro", "confeiteiro"),
                "A well-worn cooking knife", "Uma faca de cozinha bem usada"
            ),
            ProfessionItem(
                listOf("farmer", "farm", "shepherd", "herder", "agricultor", "fazendeiro", "pastor"),
                "A sturdy hand sickle", "Uma foice de mão resistente"
            ),
            ProfessionItem(
                listOf("fisher", "pescador"),
                "A coil of fishing line and hooks", "Um rolo de linha de pesca com anzóis"
            ),
            ProfessionItem(
                listOf("hunter", "hunt", "trapper", "caçador", "cacador"),
                "A set of wire snares", "Um conjunto de armadilhas de arame"
            ),
            ProfessionItem(
                listOf("bard", "musician", "minstrel", "bardo", "músic", "musico", "menestrel"),
                "A well-loved lute", "Um alaúde muito estimado"
            ),
            ProfessionItem(
                listOf("scholar", "scribe", "scrib", "estudioso", "escriba", "escrivão", "copista"),
                "A leather-bound journal", "Um diário encadernado em couro"
            ),
            ProfessionItem(
                listOf("merchant", "trader", "mercador", "comerciante", "negociante"),
                "A ledger of debts owed", "Um livro-caixa de dívidas"
            ),
            ProfessionItem(
                listOf("thief", "burglar", "cutpurse", "pickpocket", "ladrão", "ladrao", "batedor"),
                "A set of lockpicks", "Um conjunto de gazuas"
            ),
            ProfessionItem(
                listOf("carpenter", "joiner", "carpint", "marceneiro"),
                "A trusty hand saw", "Um serrote de confiança"
            ),
            ProfessionItem(
                listOf("tailor", "weaver", "seamstress", "alfaiate", "tecelão", "tecelao", "costureira"),
                "A needle and a spool of thread", "Uma agulha e um carretel de linha"
            ),
            ProfessionItem(
                listOf("guard", "soldier", "mercenary", "militia", "guarda", "soldado", "mercenário"),
                "A whetstone for the blade", "Uma pedra de amolar"
            ),
            ProfessionItem(
                listOf("miner", "quarry", "mineiro", "pedreiro"),
                "A dented iron pick", "Uma picareta de ferro amassada"
            ),
            ProfessionItem(
                listOf("sailor", "mariner", "marinheiro", "marujo"),
                "A length of strong rope", "Um bom pedaço de corda"
            ),
            ProfessionItem(
                listOf("healer", "physician", "doctor", "surgeon", "curandeiro", "médico", "medico", "cirurgião"),
                "A satchel of clean bandages", "Uma bolsa de bandagens limpas"
            )
        )

        private val WEAPONS_EN = listOf(
            "A worn dagger", "A stout wooden club", "A simple shortbow", "A rusty shortsword",
            "A sturdy quarterstaff", "A hand axe", "A light crossbow",
            "A sling and a pouch of stones", "A hunting knife", "A wooden spear"
        )
        private val WEAPONS_PT = listOf(
            "Uma adaga gasta", "Um porrete de madeira robusto", "Um arco curto simples",
            "Uma espada curta enferrujada", "Um bordão resistente", "Um machado de mão",
            "Uma besta leve", "Uma funda e uma bolsa de pedras", "Uma faca de caça",
            "Uma lança de madeira"
        )

        private val TRINKETS_EN = listOf(
            "A lucky copper coin", "A carved bone charm", "A faded love letter",
            "A small vial of cheap perfume", "A pouch of colourful marbles", "A cracked pocket mirror",
            "A dried four-leaf clover", "A worn deck of playing cards", "A tarnished silver locket",
            "A smooth river stone", "A child's wooden toy", "A half-burned candle stub",
            "A map to a place that no longer exists", "A ring that is a size too big",
            "A single mismatched earring"
        )
        private val TRINKETS_PT = listOf(
            "Uma moeda de cobre da sorte", "Um amuleto de osso entalhado", "Uma carta de amor desbotada",
            "Um pequeno frasco de perfume barato", "Uma bolsa de bolinhas de gude coloridas",
            "Um espelho de bolso trincado", "Um trevo de quatro folhas seco",
            "Um baralho de cartas gasto", "Um medalhão de prata manchado", "Uma pedra lisa de rio",
            "Um brinquedo de madeira de criança", "Um toco de vela pela metade",
            "Um mapa de um lugar que não existe mais", "Um anel um número grande demais",
            "Um brinco solitário sem par"
        )
    }
}
