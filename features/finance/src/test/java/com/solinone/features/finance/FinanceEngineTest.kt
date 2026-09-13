package com.solinone.features.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FinanceEngineTest {

    @Test
    fun testValidExpenseSms() {
        val sms = "برداشت از حساب: 50,000 ریال - مانده: 1,200,000 ریال"
        val res = FinanceSmsDetector.parseMessage(sms, "BankMelli")
        assertNotNull(res)
        assertEquals(50000L, res?.amountRial)
        assertEquals(1200000L, res?.balanceRial)
        assertEquals(TransactionType.EXPENSE, res?.type)
    }

    @Test
    fun testValidIncomeSms() {
        val sms = "واریز به حساب: 1,500,000 ریال - موجودی: 3,000,000 ریال"
        val res = FinanceSmsDetector.parseMessage(sms, "BluBank")
        assertNotNull(res)
        assertEquals(1500000L, res?.amountRial)
        assertEquals(3000000L, res?.balanceRial)
        assertEquals(TransactionType.INCOME, res?.type)
    }

    @Test
    fun testOtpRejected() {
        val sms = "رمز یکبار مصرف شما: 849201 برای انتقال مبلغ 100,000 ریال"
        val res = FinanceSmsDetector.parseMessage(sms)
        assertNull(res)
    }

    @Test
    fun testIrancellPromotionRejected() {
        val sms = "مشترک گرامی بسته اینترنت 10 گیگابایت فعال شد"
        val res = FinanceSmsDetector.parseMessage(sms)
        assertNull(res)
    }
}
