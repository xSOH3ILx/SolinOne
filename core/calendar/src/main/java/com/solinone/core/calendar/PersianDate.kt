package com.solinone.core.calendar

data class PersianDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    val monthName: String
        get() = MONTH_NAMES.getOrElse(month - 1) { "" }

    val dayOfWeekName: String
        get() = WEEK_DAY_NAMES.getOrElse(toJdn().getDayOfWeek()) { "" }

    fun toJdn(): Jdn = Jdn.fromPersian(year, month, day)

    fun formatPersian(formatDigits: Boolean = true): String {
        val str = "$year/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"
        return if (formatDigits) str.toPersianDigits() else str
    }

    companion object {
        val MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد",
            "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )

        val WEEK_DAY_NAMES = listOf(
            "شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
        )

        fun today(): PersianDate {
            val (y, m, d) = Jdn.TODAY.toPersian()
            return PersianDate(y, m, d)
        }
    }
}

fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder()
    for (char in this) {
        if (char in '0'..'9') {
            builder.append(persianDigits[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}

fun Long.toPersianRialFormatted(): String {
    val formatted = String.format("%,d", this)
    return formatted.toPersianDigits() + " ریال"
}

fun Long.toPersianTomanFormatted(): String {
    val toman = this / 10
    val formatted = String.format("%,d", toman)
    return formatted.toPersianDigits() + " تومان"
}
