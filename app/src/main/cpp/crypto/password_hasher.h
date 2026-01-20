//
// Created by Metal on 1/18/2026.
//

#ifndef PAYSMART_PASSWORD_HASHER_H
#define PAYSMART_PASSWORD_HASHER_H

#include <string>

namespace crypto {
    std::string hash_password(const std::string& password);
    bool verify_password(const std::string& password, const std::string& storedHash);
}

#endif //PAYSMART_PASSWORD_HASHER_H
