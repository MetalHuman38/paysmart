//
// Created by Metal on 1/18/2026.
//
#include "password_hasher.h"
#include "pbkdf2.h"
#include "internal_utils.h"
#include <sstream>
#include <iomanip>
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <vector>
#include <cstring>

namespace crypto {

    std::string to_hex(const std::vector<uint8_t>& data) {
        std::ostringstream oss;
        for (auto byte : data) {
            oss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(byte);
        }
        return oss.str();
    }

    std::vector<uint8_t> from_hex(const std::string& hex) {
        std::vector<uint8_t> data;
        for (size_t i = 0; i < hex.length(); i += 2) {
            std::string byteString = hex.substr(i, 2);
            auto byte = static_cast<uint8_t>(strtol(byteString.c_str(), nullptr, 16));
            data.push_back(byte);
        }
        return data;
    }

    std::string hash_password(const std::string& password) {
        std::vector<uint8_t> salt(16);

        // ✅ Safe size cast to avoid narrowing
        RAND_bytes(salt.data(), safe_size_cast(salt));

        auto hash = pbkdf2_sha256(password, salt, 150000, 32);

        return to_hex(salt) + ":" + to_hex(hash);
    }

    bool verify_password(const std::string& password, const std::string& storedHash) {
        auto pos = storedHash.find(':');
        if (pos == std::string::npos) return false;

        std::string saltHex = storedHash.substr(0, pos);
        std::string hashHex = storedHash.substr(pos + 1);

        auto salt = from_hex(saltHex);
        auto expected = from_hex(hashHex);
        auto derived = pbkdf2_sha256(password, salt, 150000, 32);

        return CRYPTO_memcmp(expected.data(), derived.data(), safe_size_cast(derived)) == 0;
    }
}
