cmake_minimum_required(VERSION 3.22.1)
project(sdjni)

# CPU-only build of stable-diffusion.cpp for Android. All GPU backends default
# OFF; we additionally disable examples, image/video file I/O (we hand raw RGB
# back over JNI), and shared libs so the static `stable-diffusion` target links
# straight into libsdjni.so. GGML_OPENMP/GGML_NATIVE off is required for the NDK
# cross-compile (no OpenMP runtime; no -march=native when cross-compiling).
set(SD_BUILD_EXAMPLES    OFF CACHE BOOL "" FORCE)
set(SD_WEBP              OFF CACHE BOOL "" FORCE)
set(SD_WEBM             OFF CACHE BOOL "" FORCE)
set(SD_BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP         OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE         OFF CACHE BOOL "" FORCE)

# CRITICAL for on-device speed: AGP compiles native code for the app's debug variant with asserts
# on and without full optimization, which makes ggml's CPU kernels 5-15x slower (a 123M CLIP encoder
# took ~30s on a Cortex-A73). Force -O3 and drop asserts (NDEBUG) for all native targets here,
# regardless of the Android build type.
add_compile_options(-O3)
add_compile_definitions(NDEBUG)

add_subdirectory(stable-diffusion.cpp)

add_library(sdjni SHARED sd_jni.cpp)
target_include_directories(sdjni PRIVATE stable-diffusion.cpp/include)
target_link_libraries(sdjni stable-diffusion log)
