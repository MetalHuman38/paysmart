package net.metalbrain.paysmart.core.security

import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel

enum class SecurityParityField(val key: String) {
    ALLOW_FEDERATED_LINKING("allowFederatedLinking"),
    PASSCODE_ENABLED("passcodeEnabled"),
    PASSWORD_ENABLED("passwordEnabled"),
    BIOMETRICS_REQUIRED("biometricsRequired"),
    BIOMETRICS_ENABLED("biometricsEnabled"),
    HAS_VERIFIED_EMAIL("hasVerifiedEmail"),
    LOCK_AFTER_MINUTES("lockAfterMinutes"),
}

data class SecurityParityResult(
    val matches: Boolean,
    val mismatches: Set<SecurityParityField>
)

object SecurityParity {
    // Keep this for future device-scoped fields; account flags should not be ignored.
    val STICKY_LOCAL_FIELDS: Set<SecurityParityField> = emptySet()

    fun signatureFromServer(model: SecuritySettingsModel?): String {
        return signature(serverSnapshot(model))
    }

    fun signatureFromLocal(model: LocalSecuritySettingsModel?): String {
        return signature(localSnapshot(model))
    }

    fun assertServerRoomParity(
        server: SecuritySettingsModel?,
        room: SecuritySettingsModel?
    ): SecurityParityResult {
        val mismatches = compare(serverSnapshot(server), serverSnapshot(room), emptySet())
        return SecurityParityResult(matches = mismatches.isEmpty(), mismatches = mismatches)
    }

    fun assertRoomLocalParity(
        room: SecuritySettingsModel?,
        local: LocalSecuritySettingsModel?,
        ignoreFields: Set<SecurityParityField> = emptySet()
    ): SecurityParityResult {
        val mismatches = compare(serverSnapshot(room), localSnapshot(local), ignoreFields)
        return SecurityParityResult(matches = mismatches.isEmpty(), mismatches = mismatches)
    }

    fun mismatchLabels(scope: String, mismatches: Set<SecurityParityField>): String {
        if (mismatches.isEmpty()) {
            return "none"
        }
        return mismatches.joinToString(",") { "$scope.${it.key}" }
    }

    private fun signature(snapshot: Map<SecurityParityField, Any?>?): String {
        if (snapshot == null) {
            return "null"
        }
        return SecurityParityField.entries.joinToString(",") { field ->
            "${field.key}=${snapshot[field]}"
        }
    }

    private fun compare(
        expected: Map<SecurityParityField, Any?>?,
        actual: Map<SecurityParityField, Any?>?,
        ignoreFields: Set<SecurityParityField>
    ): Set<SecurityParityField> {
        val comparedFields = SecurityParityField.entries.toSet() - ignoreFields

        if (expected == null && actual == null) {
            return emptySet()
        }
        if (expected == null || actual == null) {
            return comparedFields
        }

        return comparedFields.filterTo(mutableSetOf()) { field ->
            expected[field] != actual[field]
        }
    }

    private fun serverSnapshot(model: SecuritySettingsModel?): Map<SecurityParityField, Any?>? {
        if (model == null) {
            return null
        }
        return mapOf(
            SecurityParityField.ALLOW_FEDERATED_LINKING to model.allowFederatedLinking,
            SecurityParityField.PASSCODE_ENABLED to model.passcodeEnabled,
            SecurityParityField.PASSWORD_ENABLED to model.passwordEnabled,
            SecurityParityField.BIOMETRICS_REQUIRED to model.biometricsRequired,
            SecurityParityField.BIOMETRICS_ENABLED to model.biometricsEnabled,
            SecurityParityField.HAS_VERIFIED_EMAIL to model.hasVerifiedEmail,
            SecurityParityField.LOCK_AFTER_MINUTES to model.lockAfterMinutes
        )
    }

    private fun localSnapshot(model: LocalSecuritySettingsModel?): Map<SecurityParityField, Any?>? {
        if (model == null) {
            return null
        }
        return mapOf(
            SecurityParityField.ALLOW_FEDERATED_LINKING to model.allowFederatedLinking,
            SecurityParityField.PASSCODE_ENABLED to model.passcodeEnabled,
            SecurityParityField.PASSWORD_ENABLED to model.passwordEnabled,
            SecurityParityField.BIOMETRICS_REQUIRED to model.biometricsRequired,
            SecurityParityField.BIOMETRICS_ENABLED to model.biometricsEnabled,
            SecurityParityField.HAS_VERIFIED_EMAIL to model.hasVerifiedEmail,
            SecurityParityField.LOCK_AFTER_MINUTES to model.lockAfterMinutes
        )
    }
}
