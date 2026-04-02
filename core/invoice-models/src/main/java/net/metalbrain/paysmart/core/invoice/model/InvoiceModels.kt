package net.metalbrain.paysmart.core.invoice.model

data class Invoice(
    val id: String,
    val templateId: String?,
    val professionId: String? = null,
    val sections: List<InvoiceSection>,
    val lineItems: List<LineItem>,
    val totals: Totals
)

data class InvoiceSection(
    val id: String,
    val title: String,
    val fields: List<InvoiceField>,
    val order: Int
)

data class InvoiceField(
    val key: String,
    val label: String,
    val type: FieldType,
    val value: Any?,
    val required: Boolean,
    val placeholder: String?,
    val options: List<String>? = null
)

enum class FieldType {
    TEXT,
    NUMBER,
    CURRENCY,
    DATE,
    TIME,
    DURATION,
    DROPDOWN,
    BOOLEAN
}

data class LineItem(
    val id: String,
    val fields: List<InvoiceField>
)

data class Totals(
    val currencyCode: String = "GBP",
    val totalHours: Double = 0.0,
    val subtotalMinor: Long = 0L,
    val totalMinor: Long = 0L
)

data class Template(
    val id: String,
    val name: String,
    val description: String,
    val professionId: String? = null,
    val sections: List<InvoiceSection>,
    val lineItemFields: List<InvoiceField>,
    val optionalFieldKeys: Set<String> = emptySet()
)

data class Profession(
    val id: String,
    val name: String,
    val icon: String,
    val templateId: String,
    val description: String? = null
)

enum class InvoiceFormStep {
    QUICK_START,
    INVOICE_INFO,
    WORKER_DETAILS,
    CLIENT_DETAILS,
    WORK_DETAILS,
    REVIEW
}
