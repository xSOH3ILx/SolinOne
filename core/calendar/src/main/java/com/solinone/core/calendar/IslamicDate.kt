package com.solinone.core.calendar

data class IslamicDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    val monthName: String
        get() = MONTH_NAMES.getOrElse(month - 1) { "" }

    fun toJdn(): Jdn {
        val jdnLong = IranianIslamicDateConverter.toJdn(year, month, day)
        return if (jdnLong != -1L) Jdn(jdnLong) else Jdn(FallbackIslamicConverter.toJdn(year, month, day))
    }

    companion object {
        val MONTH_NAMES = listOf(
            "محرم", "صفر", "ربیع‌الاول",
            "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
            "رجب", "شعبان", "رمضان",
            "شوال", "ذی‌القعده", "ذی‌الحجه"
        )

        fun fromJdn(jdn: Jdn): IslamicDate {
            val triplet = IranianIslamicDateConverter.fromJdn(jdn.value)
                ?: FallbackIslamicConverter.fromJdn(jdn.value)
            return IslamicDate(triplet.first, triplet.second, triplet.third)
        }
    }
}

internal object IranianIslamicDateConverter {
    private const val SUPPORTED_START_JDN = 2_396_005L
    private const val SUPPORTED_START_YEAR = 1264
    private val jdSupportEnd: Long
    private val months: IntArray
    private val supportedYears: Int

    init {
        val hijriMonths = shortArrayOf(
            0b1_0_1_0_1_0_0_1_0_1_1_1.toShort(),
            0b1_0_1_0_1_0_1_0_1_0_1_0.toShort(),
            0b1_0_1_0_1_1_0_1_0_1_0_1.toShort(),
            0b0_1_0_1_1_0_0_1_0_1_0_0.toShort(),
            0b1_0_1_1_1_0_1_0_1_0_1_0.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_1_0_0_1_0_1_1_0_1_1_0.toShort(),
            0b1_0_1_0_0_1_0_1_0_1_1_1.toShort(),
            0b0_1_0_1_0_0_1_0_1_0_1_1.toShort(),
            0b0_1_1_0_1_0_1_0_0_0_1_1.toShort(),
            0b0_1_1_0_1_1_0_1_0_0_0_1.toShort(),
            0b1_0_1_0_1_1_1_0_1_0_0_1.toShort(),
            0b0_1_0_1_0_1_1_0_1_0_1_0.toShort(),
            0b1_0_1_0_0_1_1_0_1_1_0_1.toShort(),
            0b0_1_0_1_0_0_1_0_1_1_0_1.toShort(),
            0b1_1_0_0_1_0_0_1_0_1_0_1.toShort(),
            0b1_1_1_0_0_1_0_0_1_0_1_0.toShort(),
            0b1_1_1_0_1_0_1_0_0_1_0_1.toShort(),
            0b0_1_1_0_1_0_1_1_0_1_0_0.toShort(),
            0b1_0_0_1_1_0_1_1_1_0_1_0.toShort(),
            0b0_1_0_1_0_0_1_1_1_0_1_1.toShort(),
            0b0_0_1_0_0_1_0_1_1_0_1_1.toShort(),
            0b0_1_0_1_0_0_1_0_1_0_1_1.toShort(),
            0b1_0_1_0_0_1_0_1_0_1_0_1.toShort(),
            0b1_0_1_0_1_0_1_0_1_0_1_0.toShort(),
            0b1_0_1_1_0_1_0_1_1_0_0_1.toShort(),
            0b0_1_0_1_0_1_1_1_0_1_0_0.toShort(),
            0b1_0_0_1_0_1_1_1_1_0_1_0.toShort(),
            0b0_1_0_0_1_0_1_1_1_0_1_0.toShort(),
            0b1_0_1_0_0_1_0_1_1_0_1_0.toShort(),
            0b1_1_0_1_0_0_1_1_0_1_0_0.toShort(),
            0b1_1_1_0_1_0_1_1_0_0_0_1.toShort(),
            0b0_1_1_0_1_1_0_1_1_0_0_0.toShort(),
            0b1_0_1_0_1_1_1_0_1_1_0_0.toShort(),
            0b0_1_0_1_0_1_0_1_1_1_0_0.toShort(),
            0b1_0_1_0_0_1_1_0_1_1_1_0.toShort(),
            0b0_1_0_1_0_0_1_1_0_1_1_0.toShort(),
            0b1_0_1_0_1_0_0_1_1_0_1_1.toShort(),
            0b0_1_0_1_0_1_0_0_1_1_0_1.toShort(),
            0b0_1_1_0_1_0_1_0_0_1_1_0.toShort(),
            0b1_0_1_1_0_1_0_1_0_0_1_1.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_1_0_0_1_1_0_1_1_0_1_0.toShort(),
            0b1_0_1_0_0_1_1_0_1_1_0_1.toShort(),
            0b0_1_0_1_0_0_1_1_0_1_1_0.toShort(),
            0b1_0_1_0_1_0_0_1_0_1_1_1.toShort(),
            0b0_1_0_1_1_0_0_1_0_0_1_1.toShort(),
            0b1_0_1_1_0_1_0_1_0_1_0_1.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_1_0_0_1_1_1_0_1_1_0_1.toShort(),
            0b0_1_0_0_1_1_0_1_1_1_0_1.toShort(),
            0b0_1_0_0_1_0_1_1_0_1_1_1.toShort(),
            0b0_1_0_1_0_0_1_0_1_1_0_1.toShort(),
            0b1_0_1_0_1_0_0_1_0_1_1_0.toShort(),
            0b1_1_0_1_0_1_0_0_1_0_1_1.toShort(),
            0b0_1_1_0_1_1_0_0_1_0_1_0.toShort(),
            0b1_0_1_1_0_1_1_0_0_1_0_1.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_1_0_0_1_1_0_1_1_0_1_1.toShort(),
            0b0_0_1_0_0_1_1_0_1_1_0_1.toShort(),
            0b1_0_1_0_0_1_0_1_1_0_1_1.toShort(),
            0b0_1_0_1_0_0_1_0_1_1_0_1.toShort(),
            0b1_0_1_0_1_0_0_1_0_1_0_1.toShort(),
            0b1_0_1_1_0_1_0_0_1_0_1_0.toShort(),
            0b1_1_0_1_1_0_1_0_0_1_0_1.toShort(),
            0b0_1_1_0_1_1_0_1_0_1_0_0.toShort(),
            0b1_0_1_1_0_1_1_0_1_0_1_0.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_1_0_0_1_1_0_1_1_0_1_1.toShort(),
            0b0_0_1_0_0_1_1_0_1_1_0_1.toShort(),
            0b1_0_1_0_0_1_0_1_1_0_1_1.toShort(),
            0b0_1_0_1_0_0_1_0_1_1_0_1.toShort(),
            0b1_0_1_0_1_0_0_1_0_1_0_1.toShort(),
            0b1_0_1_1_0_1_0_0_1_0_1_0.toShort(),
            0b1_1_0_1_1_0_1_0_0_1_0_1.toShort(),
            0b0_1_1_0_1_1_0_1_0_1_0_0.toShort(),
            0b1_0_1_1_0_1_1_0_1_0_1_0.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_0_1_0_1_1_0_1_1_0_1_0.toShort(),
            0b1_0_0_1_0_1_1_0_1_1_0_1.toShort(),
            0b0_1_0_0_1_0_1_1_0_1_1_0.toShort(),
            0b1_0_1_0_0_1_0_1_0_1_1_1.toShort(),
            0b0_1_0_1_0_0_1_0_1_0_1_1.toShort(),
            0b1_0_1_0_1_0_0_1_0_1_0_1.toShort(),
            0b1_1_0_1_0_1_0_0_1_0_1_0.toShort(),
            0b1_1_0_1_1_0_1_0_0_1_0_1.toShort(),
            0b0_1_1_0_1_1_0_1_0_0_1_1.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_0_1_0_1_1_0_1_1_0_1_1.toShort(),
            0b0_0_1_0_0_1_1_0_1_1_0_1.toShort(),
            0b1_0_0_1_0_0_1_1_0_1_1_0.toShort(),
            0b1_0_1_0_1_0_0_1_0_1_1_1.toShort(),
            0b0_1_0_1_0_1_0_0_1_0_1_1.toShort(),
            0b1_0_1_0_1_0_1_0_0_1_0_1.toShort(),
            0b1_0_1_1_0_1_0_1_0_0_1_0.toShort(),
            0b1_1_0_1_1_0_1_0_1_0_0_1.toShort(),
            0b0_1_1_0_1_1_0_1_0_1_0_1.toShort(),
            0b0_0_1_1_0_1_1_0_1_1_0_1.toShort(),
            0b0_0_1_0_1_0_1_1_0_1_1_0.toShort(),
            0b1_0_0_1_0_0_1_1_0_1_1_1.toShort(),
            0b0_1_0_0_1_0_0_1_0_1_1_1.toShort(),
            0b0_1_1_0_0_1_0_1_0_1_0_1.toShort(),
            0b1_0_1_0_1_0_1_0_1_0_1_0.toShort(),
            0b1_0_1_1_0_1_1_0_0_1_0_1.toShort(),
            0b0_0_1_0_1_1_1_0_1_1_0_0.toShort(),
            0b1_0_0_1_0_1_1_1_0_1_0_1.toShort(),
            0b0_1_0_0_0_1_1_0_1_1_1_0.toShort(),
            0b1_0_1_0_0_0_1_1_0_1_1_0.toShort(),
            0b1_1_0_0_1_0_1_0_0_1_1_0.toShort(),
            0b1_1_0_1_0_1_0_1_0_0_1_0.toShort(),
            0b1_1_0_1_1_1_0_1_0_0_1_0.toShort(),
            0b0_1_0_1_1_1_0_1_0_1_0_1.toShort(),
            0b0_0_1_0_1_1_0_1_1_0_1_0.toShort(),
            0b0_1_0_1_0_1_0_1_1_1_0_1.toShort(),
            0b0_1_0_0_1_0_1_0_1_0_1_1.toShort(),
            0b0_1_1_0_1_0_0_1_0_0_1_1.toShort(),
            0b0_1_1_1_0_1_0_0_1_0_0_1.toShort(),
            0b0_1_1_1_1_0_1_0_0_1_0_0.toShort(),
            0b1_0_1_1_1_0_1_1_0_0_1_0.toShort(),
            0b0_1_0_1_1_0_1_1_0_1_0_1.toShort(),
            0b0_0_1_0_1_0_1_1_0_1_1_0.toShort(),
            0b0_1_1_0_0_1_0_1_1_0_1_0.toShort(),
            0b1_1_0_1_0_0_1_0_1_0_1_0.toShort(),
            0b1_1_1_0_1_0_0_1_0_1_0_0.toShort(),
            0b1_1_1_0_1_1_0_1_0_0_0_1.toShort(),
            0b0_1_1_0_1_1_1_0_1_0_0_0.toShort(),
            0b1_0_1_0_1_1_1_0_1_0_1_0.toShort(),
            0b1_0_0_1_0_1_0_1_1_1_0_0.toShort()
        )
        supportedYears = hijriMonths.size
        months = IntArray(hijriMonths.size * 12)
        var jd = 0
        repeat(hijriMonths.size * 12) { m ->
            months[m] = jd
            jd += if (hijriMonths[m / 12].toInt() shr (11 - m % 12) and 1 == 1) 30 else 29
        }
        jdSupportEnd = jd + SUPPORTED_START_JDN
    }

    fun toJdn(year: Int, month: Int, day: Int): Long {
        val yearIndex = year - SUPPORTED_START_YEAR
        return if (yearIndex !in 0 until supportedYears) -1L
        else months[yearIndex * 12 + month - 1] + day + SUPPORTED_START_JDN - 1
    }

    fun fromJdn(jd: Long): Triple<Int, Int, Int>? {
        if (jd !in SUPPORTED_START_JDN until jdSupportEnd) return null
        val days = (jd - SUPPORTED_START_JDN).toInt()
        var index = days / 30
        while (index + 1 < months.size && months[index + 1] <= days) ++index
        val yearIndex = index / 12
        val month = index % 12
        val day = days - months[index]
        return Triple(yearIndex + SUPPORTED_START_YEAR, month + 1, day + 1)
    }
}

internal object FallbackIslamicConverter {
    fun toJdn(year: Int, month: Int, day: Int): Long {
        return (day + kotlin.math.ceil(29.5 * (month - 1)) + (year - 1) * 354 + kotlin.math.floor((3 + 11 * year) / 30.0) + 1948439.0).toLong()
    }

    fun fromJdn(jd: Long): Triple<Int, Int, Int> {
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316).toInt()) * ((50 * l2 / 17719).toInt()) + ((l2 / 5670).toInt()) * ((43 * l2 / 15238).toInt())
        val l3 = l2 - (((30 - j) / 15).toInt()) * ((17719 * j / 50).toInt()) - ((j / 16).toInt()) * ((15238 * j / 43).toInt()) + 29
        val m = ((24 * l3) / 709).toInt()
        val d = (l3 - (709 * m) / 24).toInt()
        val y = 30 * n + j - 30
        return Triple(y, m, d)
    }
}
