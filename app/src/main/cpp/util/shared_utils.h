//
// Created by Metal on 1/31/2026.
//

#ifndef PAYSMART_SHARED_UTILS_H
#define PAYSMART_SHARED_UTILS_H

#pragma once

#include <string>
#include <vector>
#include <sstream>
#include <iomanip>
#include <cstdint>

namespace shared {

    inline std::string to_hex(const std::vector<uint8_t>& data) {
        std::ostringstream oss;
        for (auto byte : data) {
            oss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(byte);
        }
        return oss.str();
    }

    inline std::vector<uint8_t> from_hex(const std::string& hex) {
        if (hex.length() % 2 != 0) {
            throw std::invalid_argument("Hex string must have even length");
        }
        std::vector<uint8_t> data;
        for (size_t i = 0; i < hex.length(); i += 2) {
            std::string byteString = hex.substr(i, 2);
            auto byte = static_cast<uint8_t>(strtol(byteString.c_str(), nullptr, 16));
            data.push_back(byte);
        }
        return data;
    }

}

#endif //PAYSMART_SHARED_UTILS_H
