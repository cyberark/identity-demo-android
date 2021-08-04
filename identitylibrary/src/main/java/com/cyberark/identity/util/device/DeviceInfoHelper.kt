package com.cyberark.identity.util.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings

class DeviceInfoHelper {

    fun getDeviceName(): String {
        return Build.MODEL
    }

    fun getDeviceVersion(): String {
        return Build.VERSION.RELEASE
    }

    fun getManufacture(): String {
        return Build.MANUFACTURER
    }

    @SuppressLint("HardwareIds")
    fun getUDID(context: Context): String {
        val androidId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId
    }
}