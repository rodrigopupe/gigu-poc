package com.rpstudio.gigu_poc

import java.math.BigDecimal
import org.junit.Assert.assertEquals

/**
 * BigDecimal.equals compara a escala ("1.0" != "1.00"), então comparamos por valor.
 * `expected` é String de propósito: nunca construa BigDecimal a partir de Double.
 */
fun assertBd(expected: String, actual: BigDecimal) {
    assertEquals(
        "esperado $expected, veio $actual",
        0,
        BigDecimal(expected).compareTo(actual)
    )
}
