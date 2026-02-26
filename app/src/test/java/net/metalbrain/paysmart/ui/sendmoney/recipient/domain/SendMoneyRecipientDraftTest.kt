package net.metalbrain.paysmart.ui.sendmoney.recipient.domain

import net.metalbrain.paysmart.core.features.sendmoney.domain.BankRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.DocumentRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.EmailRequestRecipientForm
import net.metalbrain.paysmart.core.features.sendmoney.domain.RecipientMethod
import net.metalbrain.paysmart.core.features.sendmoney.domain.SendMoneyRecipientDraft
import net.metalbrain.paysmart.core.features.sendmoney.domain.VoltpayLookupRecipientForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SendMoneyRecipientDraftTest {

    @Test
    fun `normalized trims values and uppercases currencies`() {
        val normalized = SendMoneyRecipientDraft(
            sourceCurrency = " gbp ",
            targetCurrency = " usd ",
            sourceAmountInput = " 120.50 ",
            voltpayLookup = VoltpayLookupRecipientForm(
                voltTag = "  tag123 ",
                email = " mail@example.com ",
                mobile = " 07123 ",
                note = " note "
            )
        ).normalized()

        assertEquals("GBP", normalized.sourceCurrency)
        assertEquals("USD", normalized.targetCurrency)
        assertEquals("120.50", normalized.sourceAmountInput)
        assertEquals("tag123", normalized.voltpayLookup.voltTag)
        assertEquals("mail@example.com", normalized.voltpayLookup.email)
        assertEquals("07123", normalized.voltpayLookup.mobile)
        assertEquals("note", normalized.voltpayLookup.note)
    }

    @Test
    fun `selected method validation follows method-specific forms`() {
        val lookupInvalid = SendMoneyRecipientDraft(
            selectedMethod = RecipientMethod.VOLTPAY_LOOKUP,
            voltpayLookup = VoltpayLookupRecipientForm()
        )
        assertFalse(lookupInvalid.isSelectedMethodValid())

        val lookupValid = lookupInvalid.copy(
            voltpayLookup = VoltpayLookupRecipientForm(voltTag = "tag123")
        )
        assertTrue(lookupValid.isSelectedMethodValid())

        val bankInvalid = SendMoneyRecipientDraft(
            selectedMethod = RecipientMethod.BANK_DETAILS,
            bankDetails = BankRecipientForm(fullName = "Ada")
        )
        assertFalse(bankInvalid.isSelectedMethodValid())

        val bankValid = bankInvalid.copy(
            bankDetails = BankRecipientForm(
                fullName = "Ada Lovelace",
                iban = "GB82WEST12345698765432"
            )
        )
        assertTrue(bankValid.isSelectedMethodValid())

        val documentValid = SendMoneyRecipientDraft(
            selectedMethod = RecipientMethod.DOCUMENT_UPLOAD,
            documentUpload = DocumentRecipientForm(fileRef = "doc://passport/front.jpg")
        )
        assertTrue(documentValid.isSelectedMethodValid())

        val emailValid = SendMoneyRecipientDraft(
            selectedMethod = RecipientMethod.EMAIL_REQUEST,
            emailRequest = EmailRequestRecipientForm(email = "receiver@example.com")
        )
        assertTrue(emailValid.isSelectedMethodValid())
    }
}
