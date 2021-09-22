package com.cyberark.mfa

import android.app.Application
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize CyberArk Preference Util
        CyberArkPreferenceUtil.init(this)
    }
}