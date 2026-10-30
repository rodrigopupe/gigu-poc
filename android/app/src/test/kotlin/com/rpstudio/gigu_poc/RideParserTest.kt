package com.rpstudio.gigu_poc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RideParserTest {

    @Test
    fun `soma km e minutos de embarque e viagem`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "R$ 18,50",
                "Embarque: 4 min (1,2 km)",
                "Viagem: 14 min (6,0 km)",
                "Próxima oferta",
            )
        )

        assertNotNull(offer)
        assertBd("18.50", offer!!.priceBrl)
        assertBd("7.2", offer.totalKm)
        assertEquals(18, offer.totalMinutes)
    }

    @Test
    fun `processa valor com separador de milhar`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "R$ 1.250,00",
                "Embarque: 10 min (4,5 km)",
                "Viagem: 60 min (70,5 km)",
                "Próxima oferta",
            )
        )

        assertNotNull(offer)
        assertBd("1250.00", offer!!.priceBrl)
        assertBd("75.0", offer.totalKm)
        assertEquals(70, offer.totalMinutes)
    }

    @Test
    fun `processa textos com ruído misturado`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "R$ 32,00",
                "Painel\nTab 1 of 2",
                "Embarque: 3 min (1,0 km)",
                "Viagem: 12 min (7,0 km)",
                "Simulador",
                "Próxima oferta",
            )
        )

        assertNotNull(offer)
        assertBd("32.00", offer!!.priceBrl)
        assertBd("8.0", offer.totalKm)
        assertEquals(15, offer.totalMinutes)
    }

    @Test
    fun `sem valor de corrida devolve null`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "20,25",
                "Embarque: 7 min (3,0 km)",
                "Viagem: 25 min (7,5 km)",
                "Próxima oferta",
            )
        )

        assertNull(offer)
    }

    @Test
    fun `sem informacao de quilometragem devolve null`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "R$ 20,25",
                "Embarque: 7 min",
                "Viagem: 25 min",
                "Próxima oferta",
            )
        )

        assertNull(offer)
    }

    @Test
    fun `minutos ausentes valem zero`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "R$ 20,25",
                "Embarque: (3,0 km)",
                "Viagem: (7,5 km)",
                "Próxima oferta",
            )
        )

        assertNotNull(offer)
        assertEquals(0, offer!!.totalMinutes)
    }

    @Test
    fun `primeiro valor vence quando ha mais de um R$`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "R$ 25,00",
                "R$ 18,50",
                "Embarque: 4 min (1,2 km)",
                "Viagem: 14 min (6,0 km)",
                "Próxima oferta",
            )
        )

        assertNotNull(offer)
        assertBd("25.00", offer!!.priceBrl)
    }

    @Test
    fun `km zero devolve null`() {
        val offer = RideParser.parse(
            listOf(
                "Simulador de Oferta",
                "R$ 18,50",
                "Embarque: 4 min (0,0 km)",
                "Viagem: 14 min (0,0 km)",
                "Próxima oferta",
            )
        )

        // Regra C: leitura inválida não vira oferta (falha segura).
        assertNull(offer)
    }
}
