# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.
project("papersynth")

# Sets the path to the Oboe directory
set (OBOE_DIR "C:/Dev/cpp/libs/oboe")

# Adds the Oboe library as a subdirectory
add_subdirectory (${OBOE_DIR} ./oboe)

# Make the Oboe header files available to our app
include_directories (${OBOE_DIR}/include ${OBOE_DIR}/samples/shared)

# Eigen
include_directories(Eigen)

# Debug utilities
set (DEBUG_UTILS_PATH "${OBOE_DIR}/samples/debug-utils")
set (DEBUG_UTILS_SOURCES ${DEBUG_UTILS_PATH}/trace.cpp)
include_directories(${DEBUG_UTILS_PATH})

# App specific sources
set (
        APP_SOURCES
        jni-bridge.cpp
        PaperSynthOscillator.cpp
        PaperSynthSoundGenerator.cpp
        PaperSynthLatencyTuningCallback.cpp
        PaperSynthEngine.cpp
        PaperSynthMixer.cpp
        FourierSeries.cpp
        FFTUtil.cpp
        pffft/pffft.c
        pffft/fftpack.c
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.

add_library(
        papersynth SHARED
        ${DEBUG_UTILS_SOURCES}
        ${APP_SOURCES}
)

# Searches for a specified prebuilt library and stores the path as a
# variable.

find_library(log-lib log)

# Specifies libraries CMake should link to the target library.
target_link_libraries(papersynth android oboe ${log-lib})

# Enable optimization flags: if having problems with source level debugging,
# disable -Ofast ( and debug ), re-enable after done debugging.
target_compile_options(papersynth PRIVATE -Wall -Werror -DPFFFT_SIMD_DISABLE "$<$<CONFIG:RELEASE>:-Ofast>")