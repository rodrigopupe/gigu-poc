package com.rpstudio.gigu_poc

import java.math.BigDecimal
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RideEvaluateTest {

    // custo R$ 1,50/km, mínimo R$ 1,00/km líquido
    private val cost = BigDecimal("1.50")
    private val min = BigDecimal("1.00")

    @Test
    fun `oferta acima do minimo vale a pena`() {
        val offer = RideOffer(
            priceBrl = BigDecimal("18.50"),
            totalKm = BigDecimal("7.2"),
            totalMinutes = 18,
        )

        val verdict = RideParser.evaluate(offer, cost, min)

        // líquido = 18,50 - 7,2 * 1,50 = 7,70 ; por km = 7,70 / 7,2 = 1,0694 (4 casas)
        assertBd("7.70", verdict.netBrl)
        assertBd("1.0694", verdict.netPerKm)
        assertTrue(verdict.worth)
    }

    @Test
    fun `oferta ruim com liquido negativo nao compensa`() {
        val offer = RideOffer(
            priceBrl = BigDecimal("6.00"),
            totalKm = BigDecimal("11.4"),
            totalMinutes = 38,
        )

        val verdict = RideParser.evaluate(offer, cost, min)

        // líquido = 6,00 - 11,4 * 1,50 = -11,10 ; por km = -11,10 / 11,4 = -0,9737
        assertBd("-11.10", verdict.netBrl)
        assertBd("-0.9737", verdict.netPerKm)
        assertFalse(verdict.worth)
    }

    @Test
    fun `exatamente no minimo vale a pena`() {
        val offer = RideOffer(
            priceBrl = BigDecimal("25.00"),
            totalKm = BigDecimal("10.0"),
            totalMinutes = 30,
        )

        val verdict = RideParser.evaluate(offer, cost, min)

        // líquido = 25,00 - 10,0 * 1,50 = 10,00 ; por km = 1,00 = mínimo (>=, não >)
        assertBd("10.00", verdict.netBrl)
        assertBd("1.0000", verdict.netPerKm)
        assertTrue(verdict.worth)
    }

    @Test
    fun `um centavo abaixo do minimo nao compensa`() {
        val offer = RideOffer(
            priceBrl = BigDecimal("24.99"),
            totalKm = BigDecimal("10.0"),
            totalMinutes = 30,
        )

        val verdict = RideParser.evaluate(offer, cost, min)

        // líquido = 9,99 ; 9,99 < 10,00 (mínimo * km). Com Double, esse tipo de borda é frágil.
        assertFalse(verdict.worth)
    }

    @Test
    fun `km zero e entrada invalida`() {
        val offer = RideOffer(
            priceBrl = BigDecimal("20.00"),
            totalKm = BigDecimal("0.0"),
            totalMinutes = 10,
        )

        assertThrows(IllegalArgumentException::class.java) {
            RideParser.evaluate(offer, cost, min)
        }
    }
}
