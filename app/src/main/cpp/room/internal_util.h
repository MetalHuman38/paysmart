//
// Created by Metal on 1/31/2026.
//

#ifndef PAYSMART_INTERNAL_UTIL_H
#define PAYSMART_INTERNAL_UTIL_H

#pragma once

#include <cassert>
#include <cstddef>
#include <limits>

inline int safe_size_cast(std::size_t val) {
    assert(val <= static_cast<std::size_t>(std::numeric_limits<int>::max()));
    return static_cast<int>(val);
}

// Overload for string-like containers
template <typename T>
inline int safe_size_cast(const T& container) {
    return safe_size_cast(container.size());
}

#endif //PAYSMART_INTERNAL_UTIL_H
