//
// Created by Metal on 1/31/2026.
//

//
// Created by Metal on 1/31/2026.
//

#include "aes_encryptor.h"
#include "../util/shared_utils.h"
#include "internal_util.h"

#include <openssl/evp.h>
#include <openssl/rand.h>
#include <vector>
#include <sstream>
#include <iomanip>
#include <stdexcept>
#include <android/log.h>

namespace room {

    constexpr size_t IV_SIZE  = 12;  // GCM standard
    constexpr size_t TAG_SIZE = 16;

    // ---------- Key validation ----------

    bool is_valid_aes_key_size(size_t keySize) {
        return keySize == 16 || keySize == 32; // 128-bit or 256-bit
    }

    const EVP_CIPHER* select_cipher(size_t keySize) {
        if (keySize == 16) return EVP_aes_128_gcm();
        if (keySize == 32) return EVP_aes_256_gcm();
        return nullptr;
    }

    // ---------- Encryption ----------

    std::string encrypt_string(const std::string& plaintext, const std::string& keyHex) {

        std::vector<uint8_t> key = shared::from_hex(keyHex);

        if (!is_valid_aes_key_size(key.size())) {
            throw std::invalid_argument(
                    "Invalid AES key size. Only 128-bit or 256-bit keys are supported."
            );
        }

        const EVP_CIPHER* cipher = select_cipher(key.size());
        if (!cipher) {
            throw std::runtime_error("Failed to select AES cipher");
        }

        std::vector<uint8_t> iv(IV_SIZE);
        RAND_bytes(iv.data(), safe_size_cast(iv));

        std::vector<uint8_t> ciphertext(plaintext.size());
        std::vector<uint8_t> tag(TAG_SIZE);

        EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
        EVP_EncryptInit_ex(ctx, cipher, nullptr, nullptr, nullptr);
        EVP_EncryptInit_ex(ctx, nullptr, nullptr, key.data(), iv.data());

        int len;
        EVP_EncryptUpdate(
                ctx,
                ciphertext.data(),
                &len,
                reinterpret_cast<const uint8_t*>(plaintext.data()),
                safe_size_cast(plaintext.size())
        );

        int ciphertext_len = len;
        EVP_EncryptFinal_ex(ctx, ciphertext.data() + len, &len);
        ciphertext_len += len;
        ciphertext.resize(ciphertext_len);

        EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG, TAG_SIZE, tag.data());
        EVP_CIPHER_CTX_free(ctx);

        std::ostringstream oss;
        oss << shared::to_hex(iv)
            << ":" << shared::to_hex(ciphertext)
            << ":" << shared::to_hex(tag);

        return oss.str();
    }

    // ---------- Decryption ----------

    std::string decrypt_string(const std::string& input, const std::string& keyHex) {

        std::vector<uint8_t> key = shared::from_hex(keyHex);

        if (!is_valid_aes_key_size(key.size())) {
            throw std::invalid_argument(
                    "Invalid AES key size. Only 128-bit or 256-bit keys are supported."
            );
        }

        const EVP_CIPHER* cipher = select_cipher(key.size());
        if (!cipher) {
            throw std::runtime_error("Failed to select AES cipher");
        }

        size_t p1 = input.find(':');
        size_t p2 = input.find(':', p1 + 1);

        auto iv         = shared::from_hex(input.substr(0, p1));
        auto ciphertext = shared::from_hex(input.substr(p1 + 1, p2 - p1 - 1));
        auto tag        = shared::from_hex(input.substr(p2 + 1));

        EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
        EVP_DecryptInit_ex(ctx, cipher, nullptr, nullptr, nullptr);
        EVP_DecryptInit_ex(ctx, nullptr, nullptr, key.data(), iv.data());

        std::vector<uint8_t> plaintext(ciphertext.size());

        int len = 0;
        int plaintext_len = 0;

        EVP_DecryptUpdate(
                ctx,
                plaintext.data(),
                &len,
                ciphertext.data(),
                safe_size_cast(ciphertext.size())
        );

        plaintext_len = len;

        __android_log_print(ANDROID_LOG_DEBUG, "RoomNative", "🔐 GCM tag verification");

        EVP_CIPHER_CTX_ctrl(
                ctx,
                EVP_CTRL_GCM_SET_TAG,
                TAG_SIZE,
                const_cast<uint8_t*>(tag.data())
        );

        // 🔑 Step 3: final verification
        int ret = EVP_DecryptFinal_ex(
                ctx,
                plaintext.data() + plaintext_len,
                &len
        );

        __android_log_print(ANDROID_LOG_DEBUG, "RoomNative", "🔐 GCM tag verification: %d", ret);

        EVP_CIPHER_CTX_free(ctx);

        if (ret <= 0) {
            __android_log_print(ANDROID_LOG_ERROR, "RoomNative", "🔐 GCM tag verification failed");
            return "";
        }

        plaintext_len += len;

        return {
                reinterpret_cast<char*>(plaintext.data()),
                static_cast<std::string::size_type>(plaintext_len)
        };
    }
}
