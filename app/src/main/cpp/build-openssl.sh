#!/bin/bash

set -e

OPENSSL_VERSION="openssl-3.5.4"
NDK_ROOT="$ANDROID_NDK_HOME"
API=21

declare -A ABIS=(
  ["armeabi-v7a"]="arm-linux-androideabi"
  ["arm64-v8a"]="aarch64-linux-android"
  ["x86"]="i686-linux-android"
  ["x86_64"]="x86_64-linux-android"
)

cd "$OPENSSL_VERSION"

for ABI in "${!ABIS[@]}"; do
  echo "🔍 Checking build for ABI: $ABI"

  PREFIX="$(pwd)/../build/$ABI"
  LIB_CRYPTO="$PREFIX/lib/libcrypto.a"

  # ✅ Skip build if lib already exists
  if [ -f "$LIB_CRYPTO" ]; then
    echo "✅ Skipping $ABI: already built at $LIB_CRYPTO"
    echo "------------------------"
    continue
  fi

  echo "🔧 Building OpenSSL for ABI: $ABI"

  TARGET="${ABIS[$ABI]}"
  TOOLCHAIN="$NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64"

  case $ABI in
    "armeabi-v7a")
      HOST="android-arm"
      CC="$TOOLCHAIN/bin/armv7a-linux-androideabi$API-clang"
      ;;
    "arm64-v8a")
      HOST="android-arm64"
      CC="$TOOLCHAIN/bin/aarch64-linux-android$API-clang"
      ;;
    "x86")
      HOST="android-x86"
      CC="$TOOLCHAIN/bin/i686-linux-android$API-clang"
      ;;
    "x86_64")
      HOST="android-x86_64"
      CC="$TOOLCHAIN/bin/x86_64-linux-android$API-clang"
      ;;
  esac

  export AR="$TOOLCHAIN/bin/${TARGET}-ar"
  export AS="$TOOLCHAIN/bin/${TARGET}-as"
  export CC="$CC"
  export CXX="${CC/clang/clang++}"
  export LD="$TOOLCHAIN/bin/${TARGET}-ld"
  export RANLIB="$TOOLCHAIN/bin/${TARGET}-ranlib"
  export STRIP="$TOOLCHAIN/bin/${TARGET}-strip"

  ./Configure $HOST --prefix=$PREFIX no-shared no-tests no-unit-test

  make clean
  make -j$(nproc)
  make install

  echo "✅ Done building $ABI"
  echo "------------------------"
done
