package com.example.cashbacktv.viewmodel

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbacktv.data.VideoRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
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

        // cant do http requests on the ui thread, gotta launch a goroutine
        viewModelScope.launch(Dispatchers.IO) {
            sendFrames(bitmap)
        }


    }

    private fun sendFrames(bitmap: Bitmap) : Unit {
        Log.d("MainActivity", "Sending frames across net")
        val url = URL("http://3.96.152.145:3112/upload55019283475")
        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            urlConnection.doOutput = true
            urlConnection.setChunkedStreamingMode(0)
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/octet-stream")


            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)

            val out: OutputStream = BufferedOutputStream(urlConnection.outputStream)

            val byteArray = byteArrayOutputStream.toByteArray()
            Log.d("MainActivity", "${byteArray.size}")
            out.write(byteArray)
            out.flush()

            // Check response code
            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("MainActivity", "Image sent successfully")
            } else {
                Log.e("MainActivity", "Failed to send image: $responseCode")
            }


        } catch (e: Exception) {
            Log.e("MainActivity", "Error sending image: ${e}")
        } finally {
            urlConnection.disconnect()
        }
        //repo.sendData()
    }


}