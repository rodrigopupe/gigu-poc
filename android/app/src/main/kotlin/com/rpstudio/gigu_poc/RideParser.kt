package com.rpstudio.gigu_poc

import java.math.BigDecimal
import java.math.RoundingMode

// Kotlin puro: sem imports de android.* (N4), para testar na JVM.
// Dinheiro e distância em BigDecimal, sempre criados a partir de String (nunca de Double).

data class RideOffer(
    val priceBrl: BigDecimal,
    val totalKm: BigDecimal,
    val totalMinutes: Int,
)

data class Verdict(
    val worth: Boolean,
    val netBrl: BigDecimal,
    val netPerKm: BigDecimal,
)

object RideParser {

    /** Casas decimais de Verdict.netPerKm (só para exibição; a decisão não usa o valor arredondado). */
    const val NET_PER_KM_SCALE = 4

    /** F4: líquido = valor - km * custo; vale a pena se líquido/km >= mínimo. Exige totalKm > 0. */
    fun evaluate(offer: RideOffer, costPerKm: BigDecimal, minNetPerKm: BigDecimal): Verdict {
        require(offer.totalKm.signum() > 0) { "totalKm deve ser > 0" }

        val netBrl = offer.priceBrl - offer.totalKm * costPerKm
        val worth = (netBrl >= minNetPerKm * offer.totalKm)
        val netPerKm = netBrl.divide(offer.totalKm, NET_PER_KM_SCALE, RoundingMode.HALF_UP)

        return Verdict(
            worth = worth,
            netBrl = netBrl,
            netPerKm = netPerKm
        )
    }

    // "R$ 18,50" ou "R$ 1.250,00": grupo 1 = número no formato pt-BR
    private val PRICE = Regex("""R\$\s*([\d.]+,\d{2})""")

    // "(1,2 km)": grupo 1 = número dentro dos parênteses
    private val KM = Regex("""\(([\d.,]+)\s*km\)""")

    // "4 min": grupo 1 = só os dígitos
    private val MIN = Regex("""(\d+)\s*min\b""")

    /** Extrai a oferta de uma lista de textos da tela, ou null se não achar valor e km (> 0). */
    fun parse(texts: List<String>): RideOffer? {
        val joined = texts.joinToString("\n")

        val price = PRICE.find(joined)?.groupValues?.get(1)?.toBrNumber() ?: return null

        val kms = KM.findAll(joined).mapNotNull { it.groupValues[1].toBrNumber() }.toList()
        val totalKm = kms.fold(BigDecimal.ZERO, BigDecimal::add)
        // Falha segura: km zero/ausente é leitura inválida, não uma oferta.
        if (totalKm.signum() <= 0) return null

        val minutes = MIN.findAll(joined).sumOf { it.groupValues[1].toIntOrNull() ?: 0 }

        return RideOffer(priceBrl = price, totalKm = totalKm, totalMinutes = minutes)
    }

    // pt-BR: "." é milhar e "," é decimal. "1.250,00" -> BigDecimal("1250.00")
    private fun String.toBrNumber(): BigDecimal? =
        replace(".", "").replace(',', '.').toBigDecimalOrNull()
}
