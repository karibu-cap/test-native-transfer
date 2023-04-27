package com.example.myapplication

import android.app.Instrumentation
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent


class UssdRequestHandler : Service() {
    private var isRunning = false
    private val ussdCode = "#123#"
    private var step = 0
    private val responses = arrayOf("1234", "100", "1")
    private var currentResponse: String? = null
    private var ussdReceiver: BroadcastReceiver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startUssdSession()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startUssdSession() {
        val encodedUssd: String = Uri.encode(ussdCode)
        val intent = Intent("android.intent.action.CALL", Uri.parse("tel:$encodedUssd"))
        intent.putExtra("com.android.phone.extra.slot", 0)
        intent.putExtra("com.android.phone.extra.using_sim_index", 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent)
        registerReceiver(ussdReceiver, IntentFilter("com.example.myapplication.USSD_RESPONSE"))
    }

    private fun handleUssdResponse(ussdResponse: String?) {
        when (step) {
            0 -> if (ussdResponse!!.contains("Enter your PIN")) {
                currentResponse = responses[0]
                step++
            }
            1 -> if (ussdResponse!!.contains("Enter amount")) {
                currentResponse = responses[1]
                step++
            }
            2 -> if (ussdResponse!!.contains("Confirm payment?")) {
                currentResponse = responses[2]
                step++
            }
            3 -> if (ussdResponse!!.contains("Payment successful")) {
                unregisterReceiver(ussdReceiver)
                stopSelf()
            } else {
                // handle unknown messages or errors
            }
        }
    }

    private fun sendUssdResponse(response: String?) {
        try {
            val inst = Instrumentation()
            inst.sendStringSync(response)
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
        ussdReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val ussdResponse = intent.getStringExtra("ussdResponse")
                handleUssdResponse(ussdResponse)
                Log.d("TAD::UssdRequestHandler", "UssdResponse:: $ussdResponse")
                sendUssdResponse(currentResponse)
            }
        }
    }
}
