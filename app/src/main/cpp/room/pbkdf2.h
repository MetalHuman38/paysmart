//
// Created by Metal on 2/1/2026.
//

#ifndef PAYSMART_PBKDF2_H
#define PAYSMART_PBKDF2_H

#pragma once

#include <string>
#include <vector>


/**
 * Derives a key using PBKDF2-HMAC-SHA256
 * @param password Plaintext password
 * @param salt Random salt bytes
 * @param iterations Number of PBKDF2 iterations
 * @param keyLength Desired output key length in bytes
 * @return Derived key as a vector of bytes
 */

namespace room {

    std::vector<uint8_t> pbkdf2_sha256(
            const std::string& password,
            const std::vector<uint8_t>& salt,
            int iterations,
            int keyLength
    );
}



#endif //PAYSMART_PBKDF2_H
