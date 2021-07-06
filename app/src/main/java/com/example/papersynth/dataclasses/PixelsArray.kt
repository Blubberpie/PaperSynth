package com.example.papersynth.dataclasses

data class PixelsArray(val pixelsArray: IntArray, val width: Int, val height: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixelsArray

        if (!pixelsArray.contentEquals(other.pixelsArray)) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pixelsArray.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
