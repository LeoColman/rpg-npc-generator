@file:Suppress("MagicNumber")

package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import kotlin.random.Random

/**
 * The language an [ItemsGenerator] renders its item strings in. Resolved once from the active device
 * locale in the DI layer (see RandomNpcKoinModule) — the same lifetime a [FileGenerator] loads its
 * locale's raw file — so item rolls always match the app language. Any language without a translation
 * falls back to [ENGLISH].
 */
enum class ItemLocale {
    ENGLISH,
    PORTUGUESE,
    SPANISH;

    companion object {
        /** Maps an ISO 639 language code (e.g. "pt", "es") to its item locale, defaulting to English. */
        fun fromLanguage(language: String): ItemLocale = when (language) {
            "pt" -> PORTUGUESE
            "es" -> SPANISH
            else -> ENGLISH
        }
    }
}

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
 * list of lines can't express. So the tables live in code, one bundle per supported [ItemLocale]. The
 * profession table mirrors `PortraitPrompt.RACE_ANATOMY`: substring keywords in **every** language so
 * it triggers on any localized (or hand-edited) profession string. The output language is chosen by
 * [locale], which the DI layer resolves from the active locale once — the same lifetime as a
 * [FileGenerator] loading its locale's raw file.
 */
class ItemsGenerator(
    private val locale: ItemLocale,
    private val random: Random = Random.Default
) {

    private val tables: LocaleTables = TABLES.getValue(locale)

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
    fun randomTrinket(): String = pick(tables.trinkets)

    /** "Coin pouch (7 copper)" — the amount is weighted low: mostly a few copper, rarely gold. */
    private fun coinPurse(): String {
        // Weighted toward the cheapest coin (copper) so purses stay commoner-poor.
        val (denomination, maxAmount) = tables.coinDenominations.random(random)
        val amount = random.nextInt(1, maxAmount + 1)
        return "${tables.coinPrefix} ($amount $denomination)"
    }

    /**
     * The tool of the NPC's trade, or null when the profession isn't a recognised trade. Matches the
     * [PROFESSION_ITEMS] keyword table (every language) by substring, most-specific entries first.
     */
    private fun professionItem(profession: String): String? {
        if (profession.isBlank()) return null
        val key = profession.lowercase()
        val match = PROFESSION_ITEMS.firstOrNull { entry -> entry.keywords.any { it in key } }
            ?: return null
        return match.localized(locale)
    }

    private fun weapon(): String = pick(tables.weapons)

    /** [count] distinct trinkets (never a repeat within one roll). */
    private fun trinkets(count: Int): List<String> = tables.trinkets.shuffled(random).take(count)

    private fun pick(from: List<String>): String = from.random(random)

    private fun chance(probability: Double): Boolean = random.nextDouble() <= probability

    /** A profession keyword table entry: [keywords] (en + pt + es substrings) map to a localized item. */
    private data class ProfessionItem(
        val keywords: List<String>,
        val en: String,
        val pt: String,
        val es: String
    ) {
        fun localized(locale: ItemLocale): String = when (locale) {
            ItemLocale.ENGLISH -> en
            ItemLocale.PORTUGUESE -> pt
            ItemLocale.SPANISH -> es
        }
    }

    /** Every flat, per-locale item table an item roll draws from (everything but the keyword map). */
    private data class LocaleTables(
        val coinPrefix: String,
        val coinDenominations: List<Pair<String, Int>>,
        val weapons: List<String>,
        val trinkets: List<String>
    )

    companion object {
        const val MIN_ITEMS = 2
        const val MAX_ITEMS = 5
        const val MAX_TRINKETS = 2

        // A weapon shows up on a minority of NPCs — most commoners aren't armed.
        private const val WEAPON_CHANCE = 0.35

        const val COIN_PURSE_PREFIX_EN = "Coin pouch"
        const val COIN_PURSE_PREFIX_PT = "Bolsa de moedas"
        const val COIN_PURSE_PREFIX_ES = "Bolsa de monedas"

        // Denomination paired with its (low) maximum amount. Copper appears most often (it's listed
        // repeatedly) so most purses hold a handful of copper; gold is a rare windfall.
        private val TABLES: Map<ItemLocale, LocaleTables> = mapOf(
            ItemLocale.ENGLISH to LocaleTables(
                coinPrefix = COIN_PURSE_PREFIX_EN,
                coinDenominations = listOf(
                    "copper" to 20, "copper" to 20, "copper" to 20,
                    "silver" to 10, "silver" to 10,
                    "gold" to 3
                ),
                weapons = listOf(
                    "A worn dagger", "A stout wooden club", "A simple shortbow", "A rusty shortsword",
                    "A sturdy quarterstaff", "A hand axe", "A light crossbow",
                    "A sling and a pouch of stones", "A hunting knife", "A wooden spear"
                ),
                trinkets = listOf(
                    "A lucky copper coin", "A carved bone charm", "A faded love letter",
                    "A small vial of cheap perfume", "A pouch of colourful marbles",
                    "A cracked pocket mirror", "A dried four-leaf clover", "A worn deck of playing cards",
                    "A tarnished silver locket", "A smooth river stone", "A child's wooden toy",
                    "A half-burned candle stub", "A map to a place that no longer exists",
                    "A ring that is a size too big", "A single mismatched earring"
                )
            ),
            ItemLocale.PORTUGUESE to LocaleTables(
                coinPrefix = COIN_PURSE_PREFIX_PT,
                coinDenominations = listOf(
                    "de cobre" to 20, "de cobre" to 20, "de cobre" to 20,
                    "de prata" to 10, "de prata" to 10,
                    "de ouro" to 3
                ),
                weapons = listOf(
                    "Uma adaga gasta", "Um porrete de madeira robusto", "Um arco curto simples",
                    "Uma espada curta enferrujada", "Um bordão resistente", "Um machado de mão",
                    "Uma besta leve", "Uma funda e uma bolsa de pedras", "Uma faca de caça",
                    "Uma lança de madeira"
                ),
                trinkets = listOf(
                    "Uma moeda de cobre da sorte", "Um amuleto de osso entalhado",
                    "Uma carta de amor desbotada", "Um pequeno frasco de perfume barato",
                    "Uma bolsa de bolinhas de gude coloridas", "Um espelho de bolso trincado",
                    "Um trevo de quatro folhas seco", "Um baralho de cartas gasto",
                    "Um medalhão de prata manchado", "Uma pedra lisa de rio",
                    "Um brinquedo de madeira de criança", "Um toco de vela pela metade",
                    "Um mapa de um lugar que não existe mais", "Um anel um número grande demais",
                    "Um brinco solitário sem par"
                )
            ),
            ItemLocale.SPANISH to LocaleTables(
                coinPrefix = COIN_PURSE_PREFIX_ES,
                coinDenominations = listOf(
                    "de cobre" to 20, "de cobre" to 20, "de cobre" to 20,
                    "de plata" to 10, "de plata" to 10,
                    "de oro" to 3
                ),
                weapons = listOf(
                    "Una daga gastada", "Un garrote de madera robusto", "Un arco corto sencillo",
                    "Una espada corta oxidada", "Un bastón resistente", "Un hacha de mano",
                    "Una ballesta ligera", "Una honda y una bolsa de piedras", "Un cuchillo de caza",
                    "Una lanza de madera"
                ),
                trinkets = listOf(
                    "Una moneda de cobre de la suerte", "Un amuleto de hueso tallado",
                    "Una carta de amor descolorida", "Un pequeño frasco de perfume barato",
                    "Una bolsa de canicas de colores", "Un espejo de bolsillo agrietado",
                    "Un trébol de cuatro hojas seco", "Una baraja de cartas gastada",
                    "Un guardapelo de plata deslustrado", "Una piedra de río lisa",
                    "Un juguete de madera de niño", "Un cabo de vela medio quemado",
                    "Un mapa de un lugar que ya no existe", "Un anillo de una talla más grande",
                    "Un pendiente suelto sin pareja"
                )
            )
        )

        // Keyed by substring so it matches the English, Portuguese and Spanish localized profession
        // strings (and free-typed edits); order matters — more specific keywords come first.
        private val PROFESSION_ITEMS: List<ProfessionItem> = listOf(
            ProfessionItem(
                listOf("smith", "ferreiro", "ferrador", "armeiro", "herrero", "herrador", "armero"),
                "A set of smith's tools",
                "Um conjunto de ferramentas de ferreiro",
                "Un juego de herramientas de herrero"
            ),
            ProfessionItem(
                listOf("alchemist", "alquimista"),
                "A pouch of alchemical reagents",
                "Uma bolsa de reagentes alquímicos",
                "Una bolsa de reactivos alquímicos"
            ),
            ProfessionItem(
                listOf("herbalist", "herb", "herborista", "erva", "herbolario", "hierba"),
                "A bundle of dried herbs",
                "Um maço de ervas secas",
                "Un manojo de hierbas secas"
            ),
            ProfessionItem(
                listOf(
                    "priest", "cleric", "monk", "sacerdote", "padre", "monge", "clérig",
                    "monje", "párroco"
                ),
                "A holy symbol on a cord",
                "Um símbolo sagrado num cordão",
                "Un símbolo sagrado en un cordón"
            ),
            ProfessionItem(
                listOf(
                    "cook", "baker", "cozinh", "padeiro", "confeiteiro",
                    "cocin", "panadero", "repostero", "pastelero"
                ),
                "A well-worn cooking knife",
                "Uma faca de cozinha bem usada",
                "Un cuchillo de cocina muy usado"
            ),
            ProfessionItem(
                listOf(
                    "farmer", "farm", "shepherd", "herder", "agricultor", "fazendeiro", "pastor",
                    "granjero", "labrador"
                ),
                "A sturdy hand sickle",
                "Uma foice de mão resistente",
                "Una hoz de mano resistente"
            ),
            ProfessionItem(
                listOf("fisher", "pescador"),
                "A coil of fishing line and hooks",
                "Um rolo de linha de pesca com anzóis",
                "Un rollo de sedal con anzuelos"
            ),
            ProfessionItem(
                listOf("hunter", "hunt", "trapper", "caçador", "cacador", "cazador", "trampero"),
                "A set of wire snares",
                "Um conjunto de armadilhas de arame",
                "Un juego de trampas de alambre"
            ),
            ProfessionItem(
                listOf(
                    "bard", "musician", "minstrel", "bardo", "músic", "musico", "menestrel",
                    "juglar", "trovador"
                ),
                "A well-loved lute",
                "Um alaúde muito estimado",
                "Un laúd muy querido"
            ),
            ProfessionItem(
                listOf(
                    "scholar", "scribe", "scrib", "estudioso", "escriba", "escrivão", "copista",
                    "erudito", "amanuense"
                ),
                "A leather-bound journal",
                "Um diário encadernado em couro",
                "Un diario encuadernado en cuero"
            ),
            ProfessionItem(
                listOf("merchant", "trader", "mercador", "comerciante", "negociante", "mercader"),
                "A ledger of debts owed",
                "Um livro-caixa de dívidas",
                "Un libro de cuentas de deudas"
            ),
            ProfessionItem(
                listOf(
                    "thief", "burglar", "cutpurse", "pickpocket", "ladrão", "ladrao", "batedor",
                    "ladrón", "ladron", "ratero", "carterista"
                ),
                "A set of lockpicks",
                "Um conjunto de gazuas",
                "Un juego de ganzúas"
            ),
            ProfessionItem(
                listOf("carpenter", "joiner", "carpint", "marceneiro", "ebanista"),
                "A trusty hand saw",
                "Um serrote de confiança",
                "Un serrucho de confianza"
            ),
            ProfessionItem(
                listOf(
                    "tailor", "weaver", "seamstress", "alfaiate", "tecelão", "tecelao", "costureira",
                    "sastre", "tejedor", "modista", "costurera"
                ),
                "A needle and a spool of thread",
                "Uma agulha e um carretel de linha",
                "Una aguja y un carrete de hilo"
            ),
            ProfessionItem(
                listOf(
                    "guard", "soldier", "mercenary", "militia", "guarda", "soldado", "mercenário",
                    "guardia", "mercenario", "milicia"
                ),
                "A whetstone for the blade",
                "Uma pedra de amolar",
                "Una piedra de afilar la hoja"
            ),
            ProfessionItem(
                listOf("miner", "quarry", "mineiro", "pedreiro", "minero", "cantero"),
                "A dented iron pick",
                "Uma picareta de ferro amassada",
                "Un pico de hierro abollado"
            ),
            ProfessionItem(
                listOf("sailor", "mariner", "marinheiro", "marujo", "marinero", "marino"),
                "A length of strong rope",
                "Um bom pedaço de corda",
                "Un buen trozo de cuerda"
            ),
            ProfessionItem(
                listOf(
                    "healer", "physician", "doctor", "surgeon", "curandeiro", "médico", "medico",
                    "cirurgião", "sanador", "curandero", "cirujano"
                ),
                "A satchel of clean bandages",
                "Uma bolsa de bandagens limpas",
                "Un zurrón de vendas limpias"
            )
        )
    }
}
