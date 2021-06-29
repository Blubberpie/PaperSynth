package com.example.papersynth.dataclasses

data class Oscillator(val name: String?, val oscillator_data: FloatArray?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Oscillator

        if (name != other.name) return false
        if (!oscillator_data.contentEquals(other.oscillator_data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + oscillator_data.contentHashCode()
        return result
    }
}
