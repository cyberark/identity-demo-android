package com.cyberark.identity.provider

import android.content.Intent

interface CyberarkAuthInterface {

    abstract fun updateResult(intent: Intent?): Boolean
}