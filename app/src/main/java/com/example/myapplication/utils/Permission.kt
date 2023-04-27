package com.example.myapplication.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permission {
    companion object {
        private const val tag = "PermissionsUtils"
        private const val code = 99

        const val S_SMS = "android.permission.SEND_SMS"
        const val Rv_SMS = "android.permission.RECEIVE_SMS"
        const val R_P_State = "android.permission.READ_PHONE_STATE"


        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            val v1 =
                context.checkSelfPermission(permission)
            val v2 = PackageManager.PERMISSION_GRANTED
            return v1 == v2
        }

        fun checkAll(context: Context, permissions: Array<String>, make: Boolean = false, activity: Activity? = null): Boolean {
            for (element in permissions) {
                if (!isPermissionGranted(context, element)) {
                    if (make) {
                        requestPermission(activity!!, permissions, code)
                    }
                    return false
                }
            }
            return true
        }

        /**
         * Do operation on permissionResult
         *
         * @param requestCode the code of the request
         * @param permissions list of permission requested
         * @param grantResults list of grant
         * @param runnable
         * @param cancelRunnable
         * */
        fun onResult(requestCode: Int,
                     permissions: Array<String>,
                     grantResults: IntArray,
                     runnable:Runnable,
                     cancelRunnable:Runnable? = null) {
            when (requestCode) {
                code -> {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.isNotEmpty()) {
                        for (i in 0 until grantResults.count()) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                Log.e(tag, "somme permission are denied -> ${permissions[i]}")
                                return
                            }
                        }
                        Thread(runnable).start()
                    }else{
                        cancelRunnable?.run()
                    }
                    return
                }
            }

        }
        private fun requestPermission(activity: Activity, permissions: Array<String>, code: Int) {
            ActivityCompat.requestPermissions(activity, permissions, code)
        }
    }
}
