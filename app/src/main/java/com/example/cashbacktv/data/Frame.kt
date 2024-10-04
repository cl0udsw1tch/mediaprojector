package com.example.cashbacktv.data

import android.graphics.ImageFormat

data class Frame(
    var data: ByteArray,
    var width: Int,
    var height: Int,
    var timeStamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Frame

        if (!data.contentEquals(other.data)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (timeStamp != other.timeStamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + timeStamp.hashCode()
        return result
    }
}

// perhaps other image data representations that leverage an encoding other than binary?
// ...
