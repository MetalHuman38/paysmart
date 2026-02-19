//
// Created by Metal on 2/1/2026.
//


#include "room/pbkdf2.h"
#include "room/aes_encryptor.h"
#include <jni.h>
#include <string>
#include <vector>

extern "C"
JNIEXPORT jstring JNICALL
Java_net_metalbrain_paysmart_data_native_RoomNativeBridge_encryptString(
        JNIEnv* env,
        jclass clazz,
        jstring plain,
        jstring key
) {
    const char* p = env->GetStringUTFChars(plain, nullptr);
    const char* k = env->GetStringUTFChars(key, nullptr);

    try {
        std::string encrypted = room::encrypt_string(p, k);

        env->ReleaseStringUTFChars(plain, p);
        env->ReleaseStringUTFChars(key, k);
        return env->NewStringUTF(encrypted.c_str());


      }  catch (const std::invalid_argument& e) {
        env->ReleaseStringUTFChars(plain, p);
        env->ReleaseStringUTFChars(key, k);
        jclass exClass = env->FindClass("java/lang/IllegalArgumentException");
        env->ThrowNew(exClass, e.what());
        return nullptr;

    } catch (const std::exception& e) {
        env->ReleaseStringUTFChars(plain, p);
        env->ReleaseStringUTFChars(key, k);
        jclass exClass = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exClass, e.what());
        return nullptr;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_net_metalbrain_paysmart_data_native_RoomNativeBridge_decryptString(
        JNIEnv* env,
        jclass clazz,
        jstring cipher,
        jstring key
) {
    const char* c = env->GetStringUTFChars(cipher, nullptr);
    const char* k = env->GetStringUTFChars(key, nullptr);

    try {
        std::string decrypted = room::decrypt_string(c, k);
        env->ReleaseStringUTFChars(cipher, c);
        env->ReleaseStringUTFChars(key, k);
        return env->NewStringUTF(decrypted.c_str());

    } catch (const std::invalid_argument& e) {
        env->ReleaseStringUTFChars(cipher, k);
        env->ReleaseStringUTFChars(key, k);
        jclass exClass = env->FindClass("java/lang/IllegalArgumentException");
        env->ThrowNew(exClass, e.what());
        return nullptr;
} catch (const std::exception& e) {
        env->ReleaseStringUTFChars(cipher, c);
        env->ReleaseStringUTFChars(key, k);
        jclass exClass = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exClass, e.what());
        return nullptr;
    }
}



// Room Derived Key
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_net_metalbrain_paysmart_data_native_RoomNativeBridge_deriveRoomKey(
        JNIEnv *env,
        jclass thiz,
        jstring passphrase,
        jbyteArray salt,
        jint iterations,
        jint keyLen
) {
    // 🔒 Enforce key size here
    if (keyLen != 16 && keyLen != 32) {
        jclass ex = env->FindClass("java/lang/IllegalArgumentException");
        env->ThrowNew(ex, "Invalid key length. Only 128-bit or 256-bit keys are supported.");
        return nullptr;
    }

    const char *chars = env->GetStringUTFChars(passphrase, nullptr);
    std::string password(chars);
    env->ReleaseStringUTFChars(passphrase, chars);

    jsize saltLen = env->GetArrayLength(salt);
    std::vector<uint8_t> saltVec(saltLen);
    env->GetByteArrayRegion(salt, 0, saltLen, reinterpret_cast<jbyte *>(saltVec.data()));

    std::vector<uint8_t> key = room::pbkdf2_sha256(password, saltVec, iterations, keyLen);

    jbyteArray result = env->NewByteArray(keyLen);
    env->SetByteArrayRegion(result, 0, keyLen, reinterpret_cast<jbyte *>(key.data()));
    return result;
}
