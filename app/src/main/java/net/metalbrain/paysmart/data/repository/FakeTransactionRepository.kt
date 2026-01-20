package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.delay
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Transaction
import javax.inject.Inject

class FakeTransactionRepository @Inject constructor() : TransactionRepository {

    override suspend fun getTransactions(): List<Transaction> {
        delay(500) // simulate network/database delay

        return listOf(
            Transaction(
                id = "1",
                title = "To Kalejaiye Aderonke Ade...",
                time = "18:58",
                amount = -20.0,
                currency = "GBP",
                status = "Successful",
                date = "9 Jan 2026",
                iconRes = R.drawable.ic_send
            ),
            Transaction(
                id = "2",
                title = "Topup via MONZO BANK LIMI...",
                time = "18:56",
                amount = 20.0,
                currency = "GBP",
                status = "Successful",
                date = "9 Jan 2026",
                iconRes = R.drawable.ic_topup_mastercard
            ),
            Transaction(
                id = "3",
                title = "Topup via MONZO BANK LIMI...",
                time = "17:47",
                amount = 20.0,
                currency = "GBP",
                status = "Successful",
                date = "14 Nov 2025",
                iconRes = R.drawable.ic_topup_bank
            ),
            Transaction(
                id = "4",
                title = "To Kalejaiye Aderonke Ade...",
                time = "17:49",
                amount = -20.0,
                currency = "GBP",
                status = "Cancelled",
                date = "14 Nov 2025",
                iconRes = R.drawable.ic_send
            ),
            Transaction(
                id = "5",
                title = "To Wale Kalejaiye",
                time = "13:55",
                amount = -10.0,
                currency = "GBP",
                status = "Failed",
                date = "24 Oct 2025",
                iconRes = R.drawable.ic_send
            ),
        )
    }
}
