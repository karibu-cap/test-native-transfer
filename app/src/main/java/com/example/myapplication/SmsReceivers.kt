package com.example.myapplication

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast


class SmsReceivers {

    internal class OnSend(private val ctx: Context) : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val txt = "SMS_RESPONSE -> SMS sent"
                    Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show()
                    Log.d(tag, txt)
                    sentAction?.onBroadcastReceived(p1?.action ?: "", true)
                }
                else -> {
                    sentAction?.onBroadcastReceived(p1?.action ?: "", false)
                    when (resultCode) {
                        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {

                            val txt = "SMS_RESPONSE -> Generic failure"
                            Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show()
                            Log.d(tag, txt)
                        }
                        SmsManager.RESULT_ERROR_NO_SERVICE -> {

                            val txt = "SMS_RESPONSE -> No service"
                            Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show()
                            Log.d(tag, txt)
                        }
                        SmsManager.RESULT_ERROR_NULL_PDU -> {

                            val txt = "SMS_RESPONSE -> Null PDU"
                            Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show()
                            Log.d(tag, txt)
                        }
                        SmsManager.RESULT_ERROR_RADIO_OFF -> {
                            val txt = "SMS_RESPONSE -> Radio off"
                            Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show()
                            Log.d(tag, txt)
                        }
                        else -> {
                        }
                    }
                }
            }
        }

        companion object {
            private const val tag = "TAD::SmsOnReceive"
        }
    }

    internal class OnDelivered(private val ctx: Context) : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (resultCode) {
                Activity.RESULT_OK -> Toast.makeText(ctx, "SMS delivered", Toast.LENGTH_SHORT).show()
                Activity.RESULT_CANCELED -> Toast.makeText(ctx, "SMS not delivered", Toast.LENGTH_SHORT).show()
            }
        }

    }

    companion object {
        const val SEND_INTENT_ID = "SMS_GATEWAY_SENT"
        const val DELIVER_INTENT_ID = "SMS_GATEWAY_DELIVERED"

        /**
         * Register receiver for prevent sending and receiving message
         * */
        fun register(ctx: Context, sentId:String, deliverId:String) {
            ctx.registerReceiver(OnSend(ctx), IntentFilter(sentId))
            ctx.registerReceiver(OnDelivered(ctx), IntentFilter(deliverId))
        }

        @Volatile
        var sentAction: VoidCallback? = null
    }

    interface VoidCallback {
        fun onBroadcastReceived(action: String, isSent: Boolean)
    }

}

