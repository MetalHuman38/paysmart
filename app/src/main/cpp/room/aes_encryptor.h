//
// Created by Metal on 1/31/2026.
//

#ifndef PAYSMART_AES_ENCRYPTOR_H
#define PAYSMART_AES_ENCRYPTOR_H
#include <string>

namespace room {
    std::string encrypt_string(const std::string& plaintext, const std::string& key);
    std::string decrypt_string(const std::string& ciphertext, const std::string& key);
}

#endif //PAYSMART_AES_ENCRYPTOR_H
