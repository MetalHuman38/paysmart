//
// Created by Metal on 1/16/2026.
//
#include "crypto/pbkdf2.h"
#include "crypto/password_hasher.h"
#include <jni.h>
#include <string>
#include <vector>


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_net_metalbrain_paysmart_data_native_NativeBridge_deriveKeyFromCpp(
        JNIEnv *env,
        jclass,
        jstring password,
        jbyteArray salt,
        jint iterations,
        jint keyLength
) {
    const char *passwordChars = env->GetStringUTFChars(password, nullptr);
    std::string passwordStr(passwordChars);
    env->ReleaseStringUTFChars(password, passwordChars);

    jsize saltLen = env->GetArrayLength(salt);
    std::vector<uint8_t> saltVec(saltLen);
    env->GetByteArrayRegion(salt, 0, saltLen, reinterpret_cast<jbyte*>(saltVec.data()));

    std::vector<uint8_t> derivedKey = crypto::pbkdf2_sha256(passwordStr, saltVec, iterations, keyLength);

    jbyteArray result = env->NewByteArray(keyLength);
    env->SetByteArrayRegion(result, 0, keyLength, reinterpret_cast<jbyte*>(derivedKey.data()));

    return result;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_net_metalbrain_paysmart_data_native_NativePasswordBridge_hashPassword(
        JNIEnv *env,
        jclass,
        jstring password
) {
    const char *passChars = env->GetStringUTFChars(password, nullptr);
    std::string hashed = crypto::hash_password(passChars);
    env->ReleaseStringUTFChars(password, passChars);

    return env->NewStringUTF(hashed.c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_net_metalbrain_paysmart_data_native_NativePasswordBridge_verifyPassword(
        JNIEnv *env,
        jclass,
        jstring password,
        jstring storedHash
) {
    const char *passChars = env->GetStringUTFChars(password, nullptr);
    const char *hashChars = env->GetStringUTFChars(storedHash, nullptr);

    bool ok = crypto::verify_password(passChars, hashChars);

    env->ReleaseStringUTFChars(password, passChars);
    env->ReleaseStringUTFChars(storedHash, hashChars);

    return static_cast<jboolean>(ok);
}
