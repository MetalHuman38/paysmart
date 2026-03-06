const ANDROID_APK_KEY_HASH_PREFIX = "android:apk-key-hash:";
export function normalizeBase64UrlNoPadding(raw) {
    const compact = (raw || "").trim().replace(/\s+/g, "");
    if (!compact)
        return "";
    return compact
        .replace(/\+/g, "-")
        .replace(/\//g, "_")
        .replace(/=+$/g, "");
}
export function normalizeAndroidPasskeyOrigin(raw) {
    const trimmed = (raw || "").trim();
    if (!trimmed)
        return "";
    const withPrefix = trimmed.startsWith(ANDROID_APK_KEY_HASH_PREFIX)
        ? trimmed
        : `${ANDROID_APK_KEY_HASH_PREFIX}${trimmed}`;
    const hash = withPrefix.slice(ANDROID_APK_KEY_HASH_PREFIX.length);
    const normalizedHash = normalizeBase64UrlNoPadding(hash);
    if (!normalizedHash)
        return "";
    return `${ANDROID_APK_KEY_HASH_PREFIX}${normalizedHash}`;
}
export function normalizeWebPasskeyOrigin(raw) {
    const trimmed = (raw || "").trim();
    if (!trimmed)
        return "";
    if (/^https?:\/\//i.test(trimmed)) {
        return trimmed.replace(/\/+$/g, "");
    }
    return trimmed;
}
export function normalizePasskeyOrigin(raw) {
    const trimmed = (raw || "").trim();
    if (!trimmed)
        return "";
    if (trimmed.startsWith(ANDROID_APK_KEY_HASH_PREFIX)) {
        return normalizeAndroidPasskeyOrigin(trimmed);
    }
    if (/^https?:\/\//i.test(trimmed)) {
        return normalizeWebPasskeyOrigin(trimmed);
    }
    return normalizeAndroidPasskeyOrigin(trimmed);
}
export function normalizePasskeyOrigins(origins) {
    const normalized = new Set();
    for (const origin of origins) {
        const value = normalizePasskeyOrigin(origin);
        if (value) {
            normalized.add(value);
        }
    }
    return Array.from(normalized);
}
//# sourceMappingURL=passkeyOrigin.js.map