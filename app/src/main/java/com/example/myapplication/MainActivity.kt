package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.UssdResponseCallback
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.utils.Permission


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Permission.checkAll(applicationContext, arrayOf("android.permission.CALL_PHONE"), true, this)
        val mainLooper = Looper.getMainLooper();
        Thread {
//            AppWorker.init(applicationContext)
//        val serviceIntent = Intent(this, UssdRequestHandler::class.java)
//        startService(serviceIntent)

            val telephonyManager =
                getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var ussdCallback: UssdResponseCallback? = null
            ussdCallback = object : UssdResponseCallback() {
                override fun onReceiveUssdResponse(
                    telephonyManager: TelephonyManager,
                    request: String,
                    response: CharSequence
                ) {
                    Log.d("TAD::y", "request:$request  response:$response")
                    // Handle the USSD response and send predefined responses if needed
                    if (response.toString().contains("Enter your PIN")) {
                        if (ussdCallback != null && ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.CALL_PHONE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d("TAD::x", "request:$request  response:$response")
                        telephonyManager.sendUssdRequest("1234", ussdCallback, Handler(mainLooper))
                        }
                    }
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager,
                    request: String,
                    failureCode: Int
                ) {
                    // Handle USSD request failure
                }
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            telephonyManager.sendUssdRequest("#123#", ussdCallback, Handler(mainLooper))
            }
        }.start()
    }
}