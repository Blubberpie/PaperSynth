package com.example.papersynth.dataclasses

data class AlphaArray(val alphaArray: IntArray, val width: Int, val height: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlphaArray

        if (!alphaArray.contentEquals(other.alphaArray)) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = alphaArray.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
