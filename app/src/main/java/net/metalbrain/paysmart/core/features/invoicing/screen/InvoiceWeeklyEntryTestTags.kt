package net.metalbrain.paysmart.core.features.invoicing.screen

/**
 * Test tag used to identify the UI element that displays the total number of hours in an invoice.
 */
const val INVOICE_WEEKLY_LIST_TAG = "invoice_weekly_list"
const val INVOICE_FINALIZE_BUTTON_TAG = "invoice_finalize_button"
const val INVOICE_TOTAL_HOURS_TAG = "invoice_total_hours"
const val INVOICE_SUBTOTAL_TAG = "invoice_subtotal"

/**
 * Generates a unique test tag for the invoice date field at the specified [index].
 *
 * @param index The position of the date field in the list.
 * @return A string tag in the format "invoice_shift_date_{index}".
 */
fun invoiceDateFieldTag(index: Int): String = "invoice_shift_date_$index"
fun invoiceHoursFieldTag(index: Int): String = "invoice_shift_hours_$index"
