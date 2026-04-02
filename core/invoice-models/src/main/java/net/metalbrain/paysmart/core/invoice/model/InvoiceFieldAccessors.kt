package net.metalbrain.paysmart.core.invoice.model

fun Invoice.findSection(sectionId: String): InvoiceSection? = sections.firstOrNull { it.id == sectionId }

fun Invoice.findField(key: String): InvoiceField? =
    sections.asSequence().flatMap { it.fields.asSequence() }.firstOrNull { it.key == key }

fun InvoiceSection.findField(key: String): InvoiceField? = fields.firstOrNull { it.key == key }

fun LineItem.findField(key: String): InvoiceField? = fields.firstOrNull { it.key == key }

fun InvoiceField.stringValue(): String? = value as? String

fun InvoiceField.booleanValue(): Boolean? = when (val raw = value) {
    is Boolean -> raw
    is String -> raw.toBooleanStrictOrNull()
    else -> null
}

fun InvoiceField.doubleValue(): Double? = when (val raw = value) {
    is Double -> raw
    is Float -> raw.toDouble()
    is Int -> raw.toDouble()
    is Long -> raw.toDouble()
    is String -> raw.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
    else -> null
}

fun InvoiceField.longValue(): Long? = when (val raw = value) {
    is Long -> raw
    is Int -> raw.toLong()
    is Double -> raw.toLong()
    is Float -> raw.toLong()
    is String -> raw.trim().takeIf { it.isNotBlank() }?.toLongOrNull()
    else -> null
}
