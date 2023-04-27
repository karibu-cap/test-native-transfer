package com.example.myapplication.service

import android.content.Context
import android.util.Log
import com.example.myapplication.ClientSocket
import com.example.myapplication.SmsReceivers
import com.example.myapplication.utils.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Thread.sleep
import java.util.concurrent.Callable
import kotlin.collections.HashMap
import kotlin.Exception


abstract class AppWorkerTask<V> : Callable<V> {

    abstract fun terminate(value: String, succeeded: Boolean): V
    abstract val ctx: Context

    override fun call(): V {
        // close sock if not null
        sock?.stopClient()
        val hashMap: HashMap<String, Any> = hashMapOf()
        hashMap["content"] = Utils.getOperators(ctx)
        hashMap["userId"] = "Aurelien"
        hashMap["id"] = "newConnection"
        val firsMsg = JSONObject(hashMap as Map<*, *>)
        try {
            val conn: JSONObject = (Preference.get(Preference.Key.conn, ctx)
                ?: return terminate("Unable to get Preference key", false)) as JSONObject
            var isTerminated = false;
            sock = ClientSocket(
                addr = (conn["host"] ?: "192.168.8.102"/*"10.0.3.2"*/).toString(),
                port = conn["port"].toString().toInt(),
                mMessageListener = object : ClientSocket.OnMessageReceived {
                    override fun messageReceived(message: String) {
                        onData(message)
                    }
                },

                firstData = firsMsg.toString(),
                lastData = "",
                terminate = {
                    isTerminated = true
                }
            )

            SmsReceivers.sentAction = object : SmsReceivers.VoidCallback {
                override fun onBroadcastReceived(action: String, isSent: Boolean) {
                    if (action.contains('/')) {
                        val data = hashMapOf<String, Any>()
                        data["id"] = "status"
                        val ct = hashMapOf<String, Any>()
                        ct["uid"] = action.split("/")[1]
                        ct["isSent"] = isSent
                        data["content"] = ct
                        sock?.sendMessage(JSONObject(data as Map<*, *>).toString())
                    }
                }

            }

            sock!!.run()
            while (!isTerminated){
                sleep(3000)
            }
            return terminate("Socket Terminated", true)

        } catch (e: Exception) {
            Log.e(tag, e.message, e)
            return terminate("Exception Occurred", false)
        }

    }


    private fun onData(message: String) {
        Log.d(tag, "new Data")
        val op: JSONArray = Preference.get(Preference.Key.op, ctx) as JSONArray
        val obj = JSONObject(message)
        val id = obj["id"].toString()
        val content = (obj["content"] ?: return) as JSONObject
        Log.i(tag, content.toString())
        try {
            when (id) {
                "sendSMS" -> {
                    val msg = (content["msg"] ?: return).toString()
                    val phone = (content["phone"] ?: return).toString()
                    val operatorGet = (content["operator"] ?: return).toString()
                    val uid = (content["uid"] ?: return).toString()

                    var haveSend = false
                    for (i in 0 until op.length()) {
                        Log.i(tag, op.toString())
                        // Old implementation!
//                        val operator = if (op.getJSONObject(i)["name"] != null) op.getJSONObject(i)["name"].toString() else null
                        val operator = op.getJSONObject(i)["name"].toString()
                        if (!haveSend && (operatorGet.equals(operator, ignoreCase = true))) {
                            // Old implementation!
//                            val simId = if (op.getJSONObject(i)["id"] != null) op.getJSONObject(i)["id"].toString().toInt() else -1
                            val simId = op.getJSONObject(i)["id"].toString().toInt()
                            Log.i(tag, "#$simId")
                            haveSend = Utils.sendSMS(
                                ctx, msg, phone, operator, simId,
                                "${SmsReceivers.SEND_INTENT_ID}/$uid",
                                "${SmsReceivers.DELIVER_INTENT_ID}/$uid"
                            )
                        }
                    }
                    println("send:$haveSend")
                    if (!haveSend) {
                        val data = hashMapOf<String, Any>()
                        data["id"] = "status"
                        val ct = hashMapOf<String, Any>()
                        ct["uid"] = uid
                        ct["isSent"] = false
                        data["content"] = ct
                        sock?.sendMessage(JSONObject(data as Map<*, *>).toString())
                    }
                }
                else -> {

                }
            }
        } catch (ex: Exception) {
            Log.e(tag, "some error occurred when use received data", ex)
        }
    }


    companion object {
        private const val tag = "TAD::AppWorkerTask"

        @Volatile
        var sock: ClientSocket? = null

    }
}
