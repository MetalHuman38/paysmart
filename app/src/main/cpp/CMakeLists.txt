# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
cmake_minimum_required(VERSION 3.22.1)
project("paysmart")

# --- OpenSSL Configuration ---
# Set the root directory for the pre-built OpenSSL libraries for the current architecture
set(OPENSSL_ROOT_DIR ${CMAKE_SOURCE_DIR}/openssl/build/${ANDROID_ABI})

# Set the include directory for OpenSSL headers
# set(OPENSSL_INCLUDE_DIR ${OPENSSL_ROOT_DIR}/include)
set(OPENSSL_INCLUDE_DIR ${CMAKE_SOURCE_DIR}/openssl/build/${ANDROID_ABI}/include)
message(STATUS "OpenSSL include path for ${ANDROID_ABI}: ${OPENSSL_INCLUDE_DIR}")

# Define imported STATIC libraries for libcrypto and libssl
add_library(libcrypto STATIC IMPORTED)
set_target_properties(libcrypto PROPERTIES IMPORTED_LOCATION ${OPENSSL_ROOT_DIR}/lib/libcrypto.a)

add_library(libssl STATIC IMPORTED)
set_target_properties(libssl PROPERTIES IMPORTED_LOCATION ${OPENSSL_ROOT_DIR}/lib/libssl.a)


# --- crypto_utils library ---
# This is a static library for your own cryptography code that uses OpenSSL
add_library(crypto_utils STATIC
    crypto/pbkdf2.cpp crypto/password_hasher.cpp
)

# Source Directory
target_include_directories(crypto_utils PRIVATE
        ${CMAKE_SOURCE_DIR}/openssl/build/${ANDROID_ABI}/include
)

# Add the necessary include directories to crypto_utils.
# PUBLIC properties are propagated to targets that link against crypto_utils.
# This ensures that native-lib will also have these include paths.
target_include_directories(crypto_utils PRIVATE
    ${OPENSSL_INCLUDE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/crypto
        ${CMAKE_CURRENT_SOURCE_DIR}/util
)

# Link crypto_utils against the OpenSSL libraries
target_link_libraries(crypto_utils PUBLIC
    libcrypto
    libssl
)

# room_utils: native AES logic for Room DB encryption/decryption
add_library(room_utils STATIC
        room/pbkdf2.cpp room/aes_encryptor.cpp
)

# Include directory for room_utils
target_include_directories(room_utils PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/room
        ${CMAKE_CURRENT_SOURCE_DIR}/util
        ${OPENSSL_INCLUDE_DIR}
)

# Link room_utils to OpenSSL
target_link_libraries(room_utils PUBLIC
        libcrypto
        libssl
)


# --- native-lib (main shared library) ---
add_library(native-lib SHARED
    native-lib.cpp
)

# --- room-lib (main shared library) ---
add_library(room-lib SHARED
        room-lib.cpp
)

# Find Android's log library
find_library(log-lib log)

# Link native-lib against your crypto_utils library and the Android log library.
# Because crypto_utils has PUBLIC dependencies on OpenSSL, CMake will automatically
# link native-lib against OpenSSL as well.
target_link_libraries(native-lib
    crypto_utils
    ${log-lib}
)


# Link room-lib against your crypto_utils library and the Android log library.
target_link_libraries(room-lib
        room_utils
        ${log-lib}
)
