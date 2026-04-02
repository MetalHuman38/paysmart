package net.metalbrain.paysmart.core.invoice.template

import net.metalbrain.paysmart.core.invoice.model.FieldType
import net.metalbrain.paysmart.core.invoice.model.InvoiceField
import net.metalbrain.paysmart.core.invoice.model.InvoiceFieldKeys
import net.metalbrain.paysmart.core.invoice.model.InvoiceSection
import net.metalbrain.paysmart.core.invoice.model.Profession
import net.metalbrain.paysmart.core.invoice.model.Template

object InvoiceTemplateCatalog {

    val professions: List<Profession> = listOf(
        Profession(
            id = "nurse",
            name = "Nurse",
            icon = "medical_services",
            templateId = "nurse_shift_template",
            description = "Best for shift-based NHS and care work"
        ),
        Profession(
            id = "security",
            name = "Security / Door Staff",
            icon = "shield",
            templateId = "security_shift_template",
            description = "Best for guarding, door work, and site shifts"
        ),
        Profession(
            id = "freelancer",
            name = "Freelancer",
            icon = "work",
            templateId = "freelancer_template",
            description = "Best for project-based work and creative services"
        ),
        Profession(
            id = "zero_hours_worker",
            name = "Zero-hours Worker",
            icon = "schedule",
            templateId = "weekly_shift_worker_template",
            description = "Best for recurring weekly shifts and variable hours"
        ),
        Profession(
            id = "other",
            name = "Other",
            icon = "apps",
            templateId = "weekly_shift_worker_template",
            description = "Start from a flexible weekly invoice layout"
        )
    )

    val templates: List<Template> = listOf(
        weeklyShiftWorkerTemplate(),
        nurseShiftTemplate(),
        securityShiftTemplate(),
        freelancerTemplate()
    )

    fun template(templateId: String): Template? = templates.firstOrNull { it.id == templateId }

    fun profession(professionId: String): Profession? =
        professions.firstOrNull { it.id == professionId }

    private fun weeklyShiftWorkerTemplate(): Template {
        return Template(
            id = "weekly_shift_worker_template",
            name = "Weekly Shift Worker",
            description = "A simple weekly invoice for hourly and zero-hours work",
            professionId = "zero_hours_worker",
            sections = listOf(
                invoiceInfoSection(),
                workerDetailsSection(),
                clientDetailsSection()
            ),
            lineItemFields = shiftLineItemFields()
        )
    }

    private fun nurseShiftTemplate(): Template {
        return Template(
            id = "nurse_shift_template",
            name = "Nurse Shift Invoice",
            description = "Built for NHS banding, ward details, and shift extras",
            professionId = "nurse",
            sections = listOf(
                invoiceInfoSection(),
                workerDetailsSection(
                    additionalFields = listOf(
                        dropdownField(
                            key = InvoiceFieldKeys.NHS_BAND,
                            label = "NHS Band",
                            required = true,
                            options = listOf("Band 2", "Band 3", "Band 4", "Band 5", "Band 6", "Band 7", "Band 8")
                        )
                    )
                ),
                clientDetailsSection(
                    additionalFields = listOf(
                        textField(
                            key = InvoiceFieldKeys.WARD,
                            label = "Ward",
                            required = false,
                            placeholder = "Ward or department"
                        )
                    )
                )
            ),
            lineItemFields = shiftLineItemFields(
                additionalFields = listOf(
                    dropdownField(
                        key = InvoiceFieldKeys.LINE_SHIFT_TYPE,
                        label = "Shift Type",
                        required = false,
                        options = listOf("Day", "Night")
                    ),
                    currencyField(
                        key = InvoiceFieldKeys.LINE_BONUS,
                        label = "Unsocial Hours Bonus",
                        required = false,
                        placeholder = "0.00"
                    )
                )
            ),
            optionalFieldKeys = setOf(InvoiceFieldKeys.WARD, InvoiceFieldKeys.LINE_BONUS)
        )
    }

    private fun securityShiftTemplate(): Template {
        return Template(
            id = "security_shift_template",
            name = "Security / Door Staff",
            description = "For site-based guarding, door work, and event shifts",
            professionId = "security",
            sections = listOf(
                invoiceInfoSection(),
                workerDetailsSection(),
                clientDetailsSection(
                    additionalFields = listOf(
                        textField(
                            key = InvoiceFieldKeys.SITE_LOCATION,
                            label = "Site Location",
                            required = false,
                            placeholder = "Venue, site, or post"
                        )
                    )
                ),
                InvoiceSection(
                    id = "security_notes",
                    title = "Work Notes",
                    order = 3,
                    fields = listOf(
                        textField(
                            key = InvoiceFieldKeys.INCIDENT_NOTES,
                            label = "Incident Notes",
                            required = false,
                            placeholder = "Optional notes for the client"
                        )
                    )
                )
            ),
            lineItemFields = shiftLineItemFields(
                additionalFields = listOf(
                    dropdownField(
                        key = InvoiceFieldKeys.LINE_SHIFT_TYPE,
                        label = "Shift Type",
                        required = false,
                        options = listOf("Day", "Night")
                    )
                )
            ),
            optionalFieldKeys = setOf(InvoiceFieldKeys.INCIDENT_NOTES, InvoiceFieldKeys.SITE_LOCATION)
        )
    }

    private fun freelancerTemplate(): Template {
        return Template(
            id = "freelancer_template",
            name = "Freelancer",
            description = "For project work, deliverables, and hourly billing",
            professionId = "freelancer",
            sections = listOf(
                invoiceInfoSection(),
                workerDetailsSection(),
                clientDetailsSection(),
                InvoiceSection(
                    id = "project_details",
                    title = "Project Details",
                    order = 3,
                    fields = listOf(
                        textField(
                            key = InvoiceFieldKeys.PROJECT_NAME,
                            label = "Project Name",
                            required = true,
                            placeholder = "Website redesign"
                        ),
                        textField(
                            key = InvoiceFieldKeys.DESCRIPTION,
                            label = "Description",
                            required = false,
                            placeholder = "Summary of work delivered"
                        )
                    )
                )
            ),
            lineItemFields = shiftLineItemFields(
                additionalFields = listOf(
                    textField(
                        key = InvoiceFieldKeys.LINE_TASK,
                        label = "Task",
                        required = true,
                        placeholder = "Discovery workshop"
                    )
                )
            )
        )
    }

    private fun invoiceInfoSection(): InvoiceSection {
        return InvoiceSection(
            id = "invoice_info",
            title = "Invoice Info",
            order = 0,
            fields = listOf(
                textField(
                    key = InvoiceFieldKeys.INVOICE_NUMBER,
                    label = "Invoice Number",
                    required = false,
                    placeholder = "Generated automatically"
                ),
                field(
                    key = InvoiceFieldKeys.INVOICE_DATE,
                    label = "Invoice Date",
                    type = FieldType.DATE,
                    required = true,
                    placeholder = "Select invoice date"
                ),
                field(
                    key = InvoiceFieldKeys.WEEK_ENDING,
                    label = "Week Ending",
                    type = FieldType.DATE,
                    required = false,
                    placeholder = "Select week ending"
                )
            )
        )
    }

    private fun workerDetailsSection(
        additionalFields: List<InvoiceField> = emptyList()
    ): InvoiceSection {
        return InvoiceSection(
            id = "worker_details",
            title = "Worker Details",
            order = 1,
            fields = listOf(
                textField(
                    key = InvoiceFieldKeys.WORKER_NAME,
                    label = "Full Name",
                    required = true,
                    placeholder = "Full legal name"
                ),
                textField(
                    key = InvoiceFieldKeys.WORKER_ADDRESS,
                    label = "Address",
                    required = true,
                    placeholder = "Home or business address"
                ),
                textField(
                    key = InvoiceFieldKeys.WORKER_BADGE_NUMBER,
                    label = "Badge Number",
                    required = false,
                    placeholder = "Optional"
                ),
                field(
                    key = InvoiceFieldKeys.WORKER_BADGE_EXPIRY,
                    label = "Badge Expiry",
                    type = FieldType.DATE,
                    required = false,
                    placeholder = "Select badge expiry"
                ),
                textField(
                    key = InvoiceFieldKeys.WORKER_UTR,
                    label = "UTR",
                    required = false,
                    placeholder = "Unique Taxpayer Reference"
                ),
                textField(
                    key = InvoiceFieldKeys.WORKER_EMAIL,
                    label = "Email",
                    required = false,
                    placeholder = "name@example.com"
                ),
                textField(
                    key = InvoiceFieldKeys.WORKER_PHONE,
                    label = "Phone",
                    required = false,
                    placeholder = "07..."
                ),
                textField(
                    key = InvoiceFieldKeys.PAYMENT_ACCOUNT_NUMBER,
                    label = "Account Number",
                    required = false,
                    placeholder = "00000000"
                ),
                textField(
                    key = InvoiceFieldKeys.PAYMENT_SORT_CODE,
                    label = "Sort Code",
                    required = false,
                    placeholder = "00-00-00"
                ),
                textField(
                    key = InvoiceFieldKeys.PAYMENT_INSTRUCTIONS,
                    label = "Payment Instructions",
                    required = false,
                    placeholder = "Bank transfer within 7 days"
                ),
                currencyField(
                    key = InvoiceFieldKeys.DEFAULT_RATE,
                    label = "Default Rate",
                    required = false,
                    placeholder = "0.00"
                )
            ) + additionalFields
        )
    }

    private fun clientDetailsSection(
        additionalFields: List<InvoiceField> = emptyList()
    ): InvoiceSection {
        return InvoiceSection(
            id = "client_details",
            title = "Client / Venue Details",
            order = 2,
            fields = listOf(
                textField(
                    key = InvoiceFieldKeys.CLIENT_NAME,
                    label = "Client / Venue",
                    required = true,
                    placeholder = "Client or venue name"
                ),
                textField(
                    key = InvoiceFieldKeys.CLIENT_ADDRESS,
                    label = "Address",
                    required = false,
                    placeholder = "Client or venue address"
                ),
                dropdownField(
                    key = InvoiceFieldKeys.CLIENT_COUNTRY,
                    label = "Country",
                    required = false,
                    options = listOf("GB", "IE", "NG", "US", "Other")
                )
            ) + additionalFields
        )
    }

    private fun shiftLineItemFields(
        additionalFields: List<InvoiceField> = emptyList()
    ): List<InvoiceField> {
        return listOf(
            field(
                key = InvoiceFieldKeys.LINE_DATE,
                label = "Date",
                type = FieldType.DATE,
                required = true,
                placeholder = "Select shift date"
            ),
            numberField(
                key = InvoiceFieldKeys.LINE_HOURS,
                label = "Hours Worked",
                required = true,
                placeholder = "0"
            ),
            currencyField(
                key = InvoiceFieldKeys.LINE_RATE,
                label = "Rate",
                required = true,
                placeholder = "0.00"
            ),
            currencyField(
                key = InvoiceFieldKeys.LINE_AMOUNT,
                label = "Amount",
                required = false,
                placeholder = "Auto-calculated"
            )
        ) + additionalFields
    }

    private fun textField(
        key: String,
        label: String,
        required: Boolean,
        placeholder: String?
    ): InvoiceField = field(key, label, FieldType.TEXT, required, placeholder)

    private fun numberField(
        key: String,
        label: String,
        required: Boolean,
        placeholder: String?
    ): InvoiceField = field(key, label, FieldType.NUMBER, required, placeholder)

    private fun currencyField(
        key: String,
        label: String,
        required: Boolean,
        placeholder: String?
    ): InvoiceField = field(key, label, FieldType.CURRENCY, required, placeholder)

    private fun dropdownField(
        key: String,
        label: String,
        required: Boolean,
        options: List<String>
    ): InvoiceField = field(
        key = key,
        label = label,
        type = FieldType.DROPDOWN,
        required = required,
        placeholder = null,
        options = options
    )

    private fun field(
        key: String,
        label: String,
        type: FieldType,
        required: Boolean,
        placeholder: String?,
        options: List<String>? = null
    ): InvoiceField {
        return InvoiceField(
            key = key,
            label = label,
            type = type,
            value = null,
            required = required,
            placeholder = placeholder,
            options = options
        )
    }
}
