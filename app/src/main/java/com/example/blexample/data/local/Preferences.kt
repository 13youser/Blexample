package com.example.blexample.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.blexample.data.model.LeDeviceData
import com.google.gson.Gson

class Preferences private constructor(context: Context) {

    companion object {
        fun getInstance(context: Context) = Preferences(context.applicationContext)

        private const val SHARED_PREFS_DEFAULT_FILE = "BlexamplePreferences"
        private const val PREF_JSON_DEVICE_DATA = "com.example.blexample.data.PREF_JSON_DEVICE_DATA"
    }

    private val gson = Gson()
    private val prefs: SharedPreferences =
        context.applicationContext
            .getSharedPreferences(SHARED_PREFS_DEFAULT_FILE, Context.MODE_PRIVATE)


    var leDeviceData: LeDeviceData?
        get() =
            gson.fromJson(
                prefs.getString(PREF_JSON_DEVICE_DATA, ""),
                LeDeviceData::class.java
            )
        set(value) {
            prefs.edit().putString(PREF_JSON_DEVICE_DATA, gson.toJson(value)).apply()
        }
}