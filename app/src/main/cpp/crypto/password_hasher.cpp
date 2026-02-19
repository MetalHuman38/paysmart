//
// Created by Metal on 1/18/2026.
//
//
// Created by Metal on 1/18/2026.
//

#include "password_hasher.h"
#include "pbkdf2.h"
#include "../util/shared_utils.h"       // ✅ Use shared::to_hex / shared::from_hex
#include "internal_utils.h"

#include <openssl/evp.h>
#include <openssl/rand.h>
#include <vector>
#include <cstring>

namespace crypto {

    std::string hash_password(const std::string& password) {
        std::vector<uint8_t> salt(16);
        RAND_bytes(salt.data(), safe_size_cast(salt));

        auto hash = pbkdf2_sha256(password, salt, 150000, 32);

        return shared::to_hex(salt) + ":" + shared::to_hex(hash);  // ✅ Use shared helpers
    }

    bool verify_password(const std::string& password, const std::string& storedHash) {
        auto pos = storedHash.find(':');
        if (pos == std::string::npos) return false;

        std::string saltHex = storedHash.substr(0, pos);
        std::string hashHex = storedHash.substr(pos + 1);

        auto salt = shared::from_hex(saltHex);         // ✅
        auto expected = shared::from_hex(hashHex);     // ✅
        auto derived = pbkdf2_sha256(password, salt, 150000, 32);

        return CRYPTO_memcmp(expected.data(), derived.data(), safe_size_cast(derived)) == 0;
    }
}
