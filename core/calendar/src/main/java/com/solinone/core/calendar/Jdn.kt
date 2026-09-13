package com.solinone.core.calendar

import java.util.Calendar

/**
 * Julian Day Number (JDN) representation for high precision date math.
 * Standardized across SolinOne for Persian, Gregorian, and Islamic calendar conversions.
 */
@JvmInline
value class Jdn(val value: Long) : Comparable<Jdn> {
    override fun compareTo(other: Jdn): Int = value.compareTo(other.value)

    operator fun plus(days: Long): Jdn = Jdn(value + days)
    operator fun minus(days: Long): Jdn = Jdn(value - days)
    operator fun minus(other: Jdn): Long = value - other.value

    fun getDayOfWeek(): Int {
        // Saturday is 0, Friday is 6 in Persian week definition
        return ((value + 2) % 7).toInt().let { if (it < 0) it + 7 else it }
    }

    companion object {
        val TODAY: Jdn
            get() {
                val cal = Calendar.getInstance()
                return fromGregorian(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            }

        fun fromGregorian(year: Int, month: Int, day: Int): Jdn {
            val a = (14 - month) / 12
            val y = year + 4800 - a
            val m = month + 12 * a - 3
            val jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
            return Jdn(jdn.toLong())
        }

        fun fromPersian(year: Int, month: Int, day: Int): Jdn {
            val epbase = year - if (year >= 0) 474 else 473
            val epyear = 474 + (epbase % 2820)
            val md = if (month <= 7) (month - 1) * 31 else (month - 1) * 30 + 6
            val jdn = day + md + (epyear * 682 - 110) / 2816 + (epyear - 1) * 365 + epbase / 2820 * 1029983 + 1948320
            return Jdn(jdn.toLong())
        }
    }

    fun toGregorian(): Triple<Int, Int, Int> {
        val l = value + 68569
        val n = (4 * l) / 146097
        val l2 = l - (146097 * n + 3) / 4
        val i = (4000 * (l2 + 1)) / 1461001
        val l3 = l2 - (1461 * i) / 4 + 31
        val j = (80 * l3) / 2447
        val d = l3 - (2447 * j) / 80
        val l4 = j / 11
        val m = j + 2 - 12 * l4
        val y = 100 * (n - 49) + i + l4
        return Triple(y.toInt(), m.toInt(), d.toInt())
    }

    fun toPersian(): Triple<Int, Int, Int> {
        val depoch = value - fromPersian(475, 1, 1).value
        val cycle = depoch / 1029983
        val cday = depoch % 1029983
        val ycycle = if (cday == 1029982L) 2820 else ((2816 * cday + 103133) / 1028522).toInt()
        val year = 475 + cycle.toInt() * 2820 + ycycle
        val yday = value - fromPersian(year, 1, 1).value + 1
        val month: Int
        val day: Int
        if (yday <= 186) {
            month = kotlin.math.ceil(yday / 31.0).toInt()
            day = (yday - (month - 1) * 31).toInt()
        } else {
            month = kotlin.math.ceil((yday - 6) / 30.0).toInt()
            day = (yday - ((month - 1) * 30 + 6)).toInt()
        }
        return Triple(year, month, day)
    }
}
