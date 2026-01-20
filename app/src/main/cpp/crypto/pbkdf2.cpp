//
// Created by Metal on 1/17/2026.
//
#include "pbkdf2.h"
#include "internal_utils.h"
#include <openssl/evp.h>
#include <cassert>

namespace crypto {

    std::vector<uint8_t> pbkdf2_sha256(
            const std::string& password,
            const std::vector<uint8_t>& salt,
            int iterations,
            int keyLength
    ) {
        std::vector<uint8_t> key(keyLength);

        int passLen = safe_size_cast(password.length());
        int saltLen = safe_size_cast(salt.size());

        int result = PKCS5_PBKDF2_HMAC(
                password.c_str(),
                passLen,
                salt.data(),
                saltLen,
                iterations,
                EVP_sha256(),
                keyLength,
                key.data()
        );

        assert(result == 1);

        return key;
    }

}
