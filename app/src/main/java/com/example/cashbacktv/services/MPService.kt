package com.example.cashbacktv.services

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cashbacktv.MainActivity
import com.example.cashbacktv.R

class MPService : Service() {

    private val mpBinder = MPBinder()
    private lateinit var imgCallback: (data: Image) -> Unit
    private lateinit var currImage: Image
    private var lastTime = 0L


    inner class MPBinder: Binder() {
        fun getService(): MPService {
            return this@MPService
        }
    }

    fun setCallback(callback: (data: Image) -> Unit) {
        Log.d("MainActivity", "callback set")
        imgCallback = callback
    }

    fun getImage() : Image = currImage


    override fun onCreate() {
        Log.d("MainActivity", "Service created")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)


        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED)
        val resultData = intent?.getParcelableExtra<Intent>("resultData")
        var displayMetrics = resources.displayMetrics;


        var imageReader =  ImageReader.newInstance(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            PixelFormat.RGBA_8888,
            32)
        Log.d("MainActivity", "ImageReader created")


        imageReader.setOnImageAvailableListener({

            var image = imageReader.acquireLatestImage()
            if (System.currentTimeMillis() - lastTime > (1000 / 8)) {
                image?.let { img ->

                    currImage = img
    //                val updateImageIntent = Intent("UPDATE_IMAGE")
    //                sendBroadcast(updateImageIntent)
                    imgCallback(img)
                    lastTime = System.currentTimeMillis()
                }
            } else {
                image.close()
            }

        }, Handler(Looper.getMainLooper()))

        if (resultCode == Activity.RESULT_OK) {
            Log.d("MainActivity", "Permission granted")

            // Create a notification for the foreground service
            val notification = createNotification()
            startForeground(1050, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

            val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData!!)

            mediaProjection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()

                    Log.d("MediaProjectionService", "MediaProjection stopped")
                    stopSelf()
                }
            }, null)

            mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null, null
            )

            Log.d("MainActivity", "Virtual Display created")
        } else {
            Log.e("MainActivity", "Permission denied or no data received")
        }

        // want the service to restart if it's killed
        return START_STICKY
    }



    private fun createNotification(): Notification {

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.television_icon) // Decode drawable to Bitmap

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "MEDIA_PROJ")
            .setContentTitle("Screen Recording")
            .setContentText("Screen recording in progress")
            .setSmallIcon(R.drawable.television_icon)
            .setLargeIcon(largeIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setFullScreenIntent(pendingIntent, true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(false)

        return builder.build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1050, notification)
    }

    override fun onBind(intent: Intent?): IBinder {
        return mpBinder
    }
}