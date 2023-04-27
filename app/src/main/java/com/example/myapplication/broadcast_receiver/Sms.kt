package com.example.myapplication.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class Sms : BroadcastReceiver() {
    private var listener: Listener? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.e(TAG, "new receiver")
            var smsSender = ""
            var smsBody = ""
            var serviceCenter = ""
            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsSender = smsMessage.displayOriginatingAddress
                smsBody += smsMessage.messageBody
                serviceCenter = smsMessage.serviceCenterAddress ?: ""
            }
            Log.d("TAD::", "SENDER::$smsSender === BODY::$smsBody  === SERVICE::$serviceCenter")
            /*if (listener != null) {
                android.util.Log.d("TAD::", "listener::$listener")
                listener!!.onTextReceived(smsSender, smsBody)
            }*/
        }
    }


    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onTextReceived(sender: String, body: String)
    }

    init {
        /*smsBroadcastReceiver.setListener(object : SmsBroadcastReceiver.Listener {
            override fun onTextReceived(sender: String, body: String) {
                Toast.makeText(applicationContext, "from:$sender\nbody:$body", Toast.LENGTH_LONG).show()
                Log.d(tag, "SENDER::$sender === BODY::$body")
            }

        })
        applicationContext.registerReceiver(smsBroadcastReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        */
    }
    companion object {
        private val TAG = "TAD::SmsBroadcast"
    }

}
