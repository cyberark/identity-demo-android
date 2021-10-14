package com.cyberark.mfa.utils

object AppUtils {

    private var mIsForeground = false

    fun setAppOnForeground(appOnForeground: Boolean) {
        mIsForeground = appOnForeground
    }

    fun isAppOnForeground(): Boolean {
        return mIsForeground
    }
}