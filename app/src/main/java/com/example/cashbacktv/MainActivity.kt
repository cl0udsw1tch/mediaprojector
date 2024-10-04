package com.example.cashbacktv

import android.R.attr.bitmap
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.Image
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.cashbacktv.services.MPService
import com.example.cashbacktv.ui.components.CashBackTvApp
import com.example.cashbacktv.ui.theme.CashbacktvTheme
import com.example.cashbacktv.viewmodel.MPViewModel


class MainActivity : ComponentActivity() {

    private val viewmodel : MPViewModel by viewModels()

    // there are multiple ways to respond to state changes, below is more of a UI way
    // but im using livedata and a model-view-view-model architecture instead

    //private var currentBitmap: ImageBitmap? by mutableStateOf(null)
    //private var frame: Int? by mutableStateOf(0)

    private lateinit var startMediaProjection: ActivityResultLauncher<Intent>
    private lateinit var mediaProjectionManager: MediaProjectionManager
    // private lateinit var mediaProjectionReceiver: BroadcastReceiver

    private lateinit var mpService: MPService
    private lateinit var mpServiceBinder : MPService.MPBinder
    private var isBound = false

    // syntax for anonymous class .. = object [: parent] {...}
    private val mpServiceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("MainActivity","Service bound")
            mpServiceBinder = service as MPService.MPBinder
            mpServiceBinder.getService().setCallback { img: Image ->
                Log.d("MainActivity", "Activity received image. Size ${img.width}, ${img.height}")
                val planes = img.planes
                if (planes.isEmpty()) {
                    Log.e("MainActivity", "Image planes are empty")
                    img.close()
                    return@setCallback
                }

                val buffer = planes[0].buffer
                if (buffer == null) {
                    Log.e("MainActivity", "Buffer is null")
                    img.close()
                    return@setCallback
                }

                if (buffer.remaining() == 0) {
                    Log.e("MainActivity", "Converted byte array is empty")
                } else {
                    Log.d("MainActivity", "bytes not empty")
                    viewmodel.addImage(buffer,
                        width = img.width,
                        height = img.height,
                        timeStamp = System.currentTimeMillis())
                }
                img.close()
            }
            mpService = mpServiceBinder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("MainActivity", "Service unbound")
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "ONCREATE called")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

         // I need a channel for this notification
        val channel = NotificationChannel(
            "MEDIA_PROJ",
            "Media Projection",

            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for media projection"
            enableVibration(true)
            setSound(null, null)
        }

        setContent {
            CashbacktvTheme {
                CashBackTvApp(viewmodel=viewmodel, onButtonClick = { projectScreen() },
                    stopService = {
                        var intent = Intent(this, MPService::class.java)
                        stopService(intent)
                    })
            }
        }

        projectScreenSetup()

        // Register the channel with the system
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)

//
        // ================ USING A RECIEVER IS BEING PHASED OUT, SO THIS DIDNT WORK ================
//        // anonymous class, doesn't require inclusion in the manifest
//        mediaProjectionReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action == "UPDATE_IMAGE") {
//                    Log.d("MainActivity", "Activity received image.")
//                    var img = mpService.getImage()
//                    val planes = img.planes
//                    val buffer = planes[0].buffer
//                    val bytes = ByteArray(buffer.remaining())
//                    buffer.get(bytes)
//                    viewmodel.addImage(bytes,
//                        width = img.width,
//                        height = img.height,
//                        timeStamp = System.currentTimeMillis())
//                }
//            }
//        }
//
//        // Register the receiver
//        val intentFilter = IntentFilter("UPDATE_IMAGE")
//        registerReceiver(
//            mediaProjectionReceiver,
//            intentFilter,
//            RECEIVER_NOT_EXPORTED
//        )
//        sendBroadcast(Intent("UPDATE_IMAGE"))

    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MPService::class.java)
        bindService(intent, mpServiceConn, 0)
    }

    override fun onStop() {
        Log.d("MainActivity", "Activity ONSTOP called")
        super.onStop()
    }

    override fun onPause() {
        Log.d("MainActivity", "Activity ONPAUSE called")
        if (isBound) {
            mpService.setCallback { Log.d("MainActivity", "Thought you were gonna crash my app huh :*(") }
            unbindService(mpServiceConn)
        }
        super.onPause()
    }

    override fun onResume() {
        Log.d("MainActivity", "Activity ONRESUME called")
        super.onResume()

        val intent = Intent(this, MPService::class.java)
        bindService(intent, mpServiceConn, 0)
    }

    override fun onDestroy() {
        Log.d("MainActivity", "ONDESTROY called.")

        super.onDestroy()
    }
    private fun projectScreenSetup() {

//        val displayMetrics = resources.displayMetrics

        // this is a system service.
        // Can create the intent to start the projection activity
        // can retrieve media projection tokens
        mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)

        // "A token granting applications the ability to capture screen contents and/or
        // record system audio. The exact capabilities granted depend on the type of
        // MediaProjection."
        // i.e pretty much a handler. Can use this if we want to start a projection
        // whenever, wherever
//        var mediaProjection: MediaProjection

        // handler, asynchronous when started, pass both a
        // - contract to determine what do
        // do when the activity is launched (request permission(s)? return something? literally
        // a 'contract' between the two activities)
        // - callback to handle the result.
        // allows the generated activity to request permissions and return a result
        // the permission is specified by the intent during launch
//        val serviceIntent = Intent(this, MPService::class.java)
//        serviceIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        // Start the media projection activity, gets the result, if the result is OK, then
        // starts the service. The service needs the permission in the context of the activity
        // to start so startForeGroundService MUST be called inside the check for the result
        startMediaProjection = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val serviceIntent = Intent(this, MPService::class.java)
                // This part took a while to understand, but services can be restarted independently
                // of the parent activity, so we need to actually give the service its own results
                // so it too can perform the check on resultCode. Furthermore, its required
                // because the mediaprojection actually needs the resultCode and resultData
                serviceIntent.putExtra("resultCode", result.resultCode)
                serviceIntent.putExtra("resultData", result.data) // Pass the data to the service
                startForegroundService(serviceIntent)
                bindService(intent, mpServiceConn, 0)
                Log.d("MainActivity", "Successfully started service")
            }
        }
    }

    private fun projectScreen() : Unit {
        // start the activity, which will execute the callback on granted permissions
        // here the intent is specified, and the user will be notified of the permission
        // the application wants granted
        Log.d("MainActivity", "Projection started..")
        startMediaProjection.launch(mediaProjectionManager.createScreenCaptureIntent())
    }
}


