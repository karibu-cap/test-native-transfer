package com.example.myapplication.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

class Utils {
    companion object {
        private const val tag = "TAD::Utils::"

        /**
         * Return the list of mobile operator if exist.
         *
         * @param ctx the [Context] of the application
         * @param call represent the method to call for each operator get
         * */
        fun getOperators(
            ctx: Context,
            call: (HashMap<String, String>) -> Any? = { _ -> null }
        ): ArrayList<HashMap<String, String>> {
            var doOldMethod = true
            val res: ArrayList<HashMap<String, String>> = arrayListOf()
            val simManager: SubscriptionManager =
                ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                    ctx,
                    android.Manifest.permission.READ_PHONE_STATE
                )
            ) {
                Log.e(tag, "permission denied")
                return res
            }
            val simCount: Int = simManager.activeSubscriptionInfoCount
            if (simCount > 1) {
                doOldMethod = false
                val simOpeatorList = simManager.activeSubscriptionInfoList
                var idx = 0
                while (idx < simCount) {
                    println(simOpeatorList[idx])
                    val op: HashMap<String, String> = hashMapOf()
                    op["id"] = simOpeatorList[idx].subscriptionId.toString()
                    op["name"] = simOpeatorList[idx].carrierName.toString()
                    op["country"] = simOpeatorList[idx].countryIso.toString()
                    op["displayName"] = simOpeatorList[idx].displayName.toString()
                    op["simSlotIndex"] = simOpeatorList[idx].simSlotIndex.toString()
                    res.add(op)
                    call(op)
                    idx++
                }
            }
            if (doOldMethod) {
                val telephony: TelephonyManager =
                    ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                val state = telephony.simState
                if (state == TelephonyManager.SIM_STATE_READY) {
                    val op: HashMap<String, String> = HashMap()
                    op["id"] = "-1"
                    op["name"] = telephony.simOperatorName
                    op["country"] = telephony.simCountryIso
                    call(op)
                    res.add(op)
                }
            }

            return res
        }

    }
}