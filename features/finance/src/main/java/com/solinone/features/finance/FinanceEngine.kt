package com.solinone.features.finance

import com.solinone.core.calendar.toPersianDigits
import java.util.regex.Pattern

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class DetectedTransaction(
    val amountRial: Long,
    val type: TransactionType,
    val balanceRial: Long?,
    val confidenceScore: Int,
    val sender: String,
    val rawText: String
)

object FinanceSmsDetector {

    private val EXPENSE_KEYWORDS = listOf(
        "برداشت", "خرید", "پرداخت", "کارمزد", "پایانه", "انتقال", "حواله", "کاهش", "قسط", "پرید", "بدهکار"
    )

    private val INCOME_KEYWORDS = listOf(
        "واریز", "انتقالی", "بستانکار", "دریافت", "افزایش", "حقوق", "وصول", "نشست", "بازپرداخت"
    )

    private val OTP_KEYWORDS = listOf(
        "رمز پویا", "رمز یکبار مصرف", "کد تایید", "کد تأیید", "کد فعالسازی", "رمز عبور"
    )

    private val OPERATOR_KEYWORDS = listOf(
        "بسته اینترنت", "شارژ", "مشترک گرامی", "گیگابایت", "مگابایت", "اعتبار شما"
    )

    // Match 1 to 3 digits followed by comma/dot separated 3 digits
    private val GROUPED_NUMBER_PATTERN = Pattern.compile("\\b\\d{1,3}(?:[,،.]\\d{3})+\\b")

    fun parseMessage(rawText: String, sender: String = ""): DetectedTransaction? {
        val normalized = normalize(rawText)

        // Gate 1: Check OTP and Operator messages
        if (OTP_KEYWORDS.any { normalized.contains(it) }) return null
        if (OPERATOR_KEYWORDS.any { normalized.contains(it) } && !normalized.contains("مانده") && !normalized.contains("موجودی")) {
            return null
        }

        // Gate 2: Direction Resolution
        val isExpense = EXPENSE_KEYWORDS.any { normalized.contains(it) } || normalized.contains("-")
        val isIncome = INCOME_KEYWORDS.any { normalized.contains(it) } || normalized.contains("+")

        if (!isExpense && !isIncome) return null
        val type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME

        // Gate 3: Extract Amount
        val matcher = GROUPED_NUMBER_PATTERN.matcher(normalized)
        val numbers = mutableListOf<Long>()
        while (matcher.find()) {
            val numStr = matcher.group().replace(",", "").replace("،", "").replace(".", "")
            numStr.toLongOrNull()?.let { numbers.add(it) }
        }

        if (numbers.isEmpty()) return null

        val amount = numbers.first()
        val balance = if (numbers.size > 1) numbers[1] else null

        var score = 60
        if (normalized.contains("مانده") || normalized.contains("موجودی")) score += 25
        if (numbers.size > 1) score += 15

        return DetectedTransaction(
            amountRial = amount,
            type = type,
            balanceRial = balance,
            confidenceScore = score,
            sender = sender,
            rawText = rawText
        )
    }

    private fun normalize(text: String): String {
        return text
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('۰', '0')
            .replace('۱', '1')
            .replace('۲', '2')
            .replace('۳', '3')
            .replace('۴', '4')
            .replace('۵', '5')
            .replace('۶', '6')
            .replace('۷', '7')
            .replace('۸', '8')
            .replace('۹', '9')
    }
}
