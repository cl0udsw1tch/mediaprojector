package com.example.cashbacktv.data

import androidx.compose.ui.graphics.ImageBitmap

class VideoRepo {

    private var frames = mutableListOf<Frame>()
    private var count: Int = 0

    public enum class ResponseCode {
        OK, BAD
    }

    fun addFrame(image: ByteArray, width: Int, height: Int, timeStamp: Long) : Unit {

        // for simplicity im assuming one plane, like a JPEG would have

        frames.add(Frame(
            data = image,
            width = width,
            height = height,
            timeStamp = timeStamp,
        ))
        count++
    }

    fun getFrames() : MutableList<Frame> {
        return frames
    }
    fun getCount() : Int {
        return count
    }


    fun sendData() : ResponseCode {



        // logic to send across HTTP



        return ResponseCode.OK
    }
}