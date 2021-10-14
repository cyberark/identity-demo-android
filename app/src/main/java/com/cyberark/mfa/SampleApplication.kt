/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyberark.mfa

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.utils.AppUtils

class SampleApplication : Application(), LifecycleObserver {

    companion object {
        private val TAG = SampleApplication::class.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize CyberArk Preference Util
        CyberArkPreferenceUtil.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        Log.i(TAG, "App in background")
        // App in background
        AppUtils.setAppOnForeground(false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        Log.i(TAG, "App in foreground")
        // App in foreground
        AppUtils.setAppOnForeground(true)
    }
}