package com.example.papersynth.enums

enum class MusicalScale {
    CHROMATIC, // 88
    WHOLE_TONE, // 2-2-2-2-2-2 41
    DIATONIC, // 2-2-1-2-2-2-1 49
    HARMONIC_MINOR, // 2-1-2-2-1-3-1 49
    BYZANTINE, // 1-3-1-2-1-3-1 49
    PENTATONIC, // 2-2-3-2-3 35
    AKEBONO, // 2-1-4-1-4 35
    BLUES_HEXATONIC, // 3-2-1-1-3-2 41
    HUNGARIAN_MINOR, // 2-1-3-1-1-3-1 49
    NUM_SCALES;

    companion object {
        fun getStrings(): Array<String> {
            val strings = Array(NUM_SCALES.ordinal) { "" }
            values().forEachIndexed { i, scale ->
                if (i != NUM_SCALES.ordinal) {
                    strings[i] = scale.name
                }
            }
            return strings
        }
    }
}