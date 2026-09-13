package com.solinone.features.finance.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.solinone.core.calendar.PersianDate
import com.solinone.core.database.SolinOneDatabase
import com.solinone.core.database.entity.TransactionEntity
import com.solinone.features.finance.FinanceSmsDetector
import com.solinone.features.finance.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val fullBody = messages.joinToString(separator = "") { it.displayMessageBody ?: "" }
        val sender = messages.firstOrNull()?.displayOriginatingAddress ?: ""

        val detected = FinanceSmsDetector.parseMessage(fullBody, sender) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SolinOneDatabase.getDatabase(context)
                val todayPersian = PersianDate.today().formatPersian(formatDigits = false)

                val entity = TransactionEntity(
                    amountRial = detected.amountRial,
                    type = if (detected.type == TransactionType.INCOME) "INCOME" else "EXPENSE",
                    category = "بانکی خودکار",
                    note = detected.sender,
                    timestamp = System.currentTimeMillis(),
                    persianDate = todayPersian,
                    isPendingReview = true,
                    sender = detected.sender
                )
                db.transactionDao().insertTransaction(entity)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
