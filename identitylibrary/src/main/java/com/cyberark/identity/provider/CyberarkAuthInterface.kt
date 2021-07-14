package com.cyberark.identity.provider

import android.content.Intent

internal interface CyberarkAuthInterface {

    abstract fun updateResult(intent: Intent?): Boolean
}