package com.example.myapplication.utils


import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class Preference {
    internal class Key {
        companion object {
            const val conn = "connections"
            const val op = "operators"
        }
    }

    companion object {
        private const val PREFS_FILE = "com.example.myapplication.pref_file"

        @Volatile
        private var prefs: SharedPreferences? = null

        private fun getPrefs(ctx: Context): SharedPreferences {
            synchronized(this) {
                if (prefs == null) {
                    prefs = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                }
                return prefs!!
            }
        }

        fun set(key: String, value: Any, ctx: Context) {
            val data = when (value) {
                is ArrayList<*> -> "${JSONArray(value)}\\@A"
                is Boolean -> "$value\\@B"
                is Double -> "$value\\@D"
                is HashMap<*, *> -> "${JSONObject(value)}\\@H"
                is Int -> "$value\\@I"
                else -> "$value\\@S"
            }
            getPrefs(ctx).edit().putString(key, data).apply()
        }

        fun get(key: String, ctx: Context, default:Any? = null): Any? {
            synchronized(this) {
                val res: String? = getPrefs(ctx).getString(key, "")
                if (res == null || res.length <= 3){
                    return default
                }
                val hashIndex = res.length - 3;
                return when ( res.substring(hashIndex, res.length)) {
                    "\\@A" -> JSONArray(res.substring(0, hashIndex))
                    "\\@B" -> res.substring(0, ).toBoolean()
                    "\\@D" -> res.substring(0, hashIndex).toDouble()
                    "\\@H" -> JSONObject(res.substring(0, hashIndex))
                    "\\@I" -> res.substring(0, hashIndex).toInt()
                    "\\@S" -> res.substring(0, hashIndex)
                    else -> default
                }
            }
        }
    }
}