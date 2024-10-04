package com.example.cashbacktv.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cashbacktv.data.VideoRepo
import java.nio.ByteBuffer


// this will be specifically for the ui to interact with
class MPViewModel : ViewModel() {

    private var repo: VideoRepo = VideoRepo()
    private val _count = MutableLiveData<Int>(repo.getCount())
    val count: LiveData<Int> get() = _count

    private val _currImage = MutableLiveData<ImageBitmap>()
    val currImage : LiveData<ImageBitmap> = _currImage

    fun addImage(imageBuffer: ByteBuffer,width: Int, height: Int, timeStamp: Long): Unit {
        Log.d("MainActivity", "ViewModel: Adding image")
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        var byteArr: ByteArray = ByteArray(imageBuffer.remaining())
        imageBuffer.get(byteArr)
        //repo.addFrame(byteArr, width, height, timeStamp)

        var newBuff : ByteBuffer = ByteBuffer.wrap(byteArr)
        bitmap.copyPixelsFromBuffer(newBuff)
        _currImage.value = bitmap.asImageBitmap()
        _count.value = _count.value?.plus(1)


    }

    fun sendFrames() : Unit {
        repo.sendData()
    }


}