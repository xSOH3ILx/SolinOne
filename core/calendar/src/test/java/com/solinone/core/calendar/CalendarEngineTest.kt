package com.solinone.core.calendar

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarEngineTest {

    @Test
    fun testPersianToGregorianAndBack() {
        // Nowruz 1403 = March 20, 2024
        val jdn1403 = Jdn.fromPersian(1403, 1, 1)
        val (gy, gm, gd) = jdn1403.toGregorian()
        assertEquals(2024, gy)
        assertEquals(3, gm)
        assertEquals(20, gd)

        val jdnGreg = Jdn.fromGregorian(2024, 3, 20)
        val (py, pm, pd) = jdnGreg.toPersian()
        assertEquals(1403, py)
        assertEquals(1, pm)
        assertEquals(1, pd)
    }

    @Test
    fun testPersianDigitsConversion() {
        val input = "1403/01/01 - 50,000"
        val expected = "۱۴۰۳/۰۱/۰۱ - ۵۰,۰۰۰"
        assertEquals(expected, input.toPersianDigits())
    }
    @Test
    fun testIslamicConversion() {
        // 1403/1/1 = 2024-03-20 = 1445/09/09 Ramadan
        val jdn = Jdn.fromPersian(1403, 1, 1)
        val islamic = IslamicDate.fromJdn(jdn)
        assertEquals(1445, islamic.year)
        assertEquals(9, islamic.month)
        assertEquals(9, islamic.day)
    }
}
