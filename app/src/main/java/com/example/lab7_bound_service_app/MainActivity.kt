package com.example.lab7_bound_service_app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this
        
        start_service_button.setOnClickListener { requestDownload() }
    }

    private var mService: Messenger? = null
    private var mBound = false

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, DownloadImageService::class.java),
            mConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
    }

    private var mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = Messenger(service)
            mBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
            mBound = false
        }
    }

    private fun requestDownload() {
        if (!mBound) return
        val msg = Message.obtain(null, DownloadImageService.MSG_REQUEST_PATH)
        val b = Bundle()
        b.putString("URL", edittext_with_url.text.toString())
        msg.data = b
        msg.replyTo = Messenger(ResponseHandler())
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    class ResponseHandler : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == DownloadImageService.MSG_REQUEST_PATH_RESPONSE) {
                activity.textview_for_path.text = msg.data.getString("PATH")
            } else {
                super.handleMessage(msg)
            }
        }
    }

    companion object {
        private lateinit var activity: MainActivity
    }
}
