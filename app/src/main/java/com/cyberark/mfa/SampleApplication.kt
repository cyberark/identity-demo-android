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
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.utils.AppUtils

class SampleApplication : Application(), LifecycleEventObserver {

    companion object {
        private val TAG = SampleApplication::class.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize CyberArk Preference Util
        CyberArkPreferenceUtil.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                Log.i(TAG, "App in background")
                // App in background
                AppUtils.setAppOnForeground(false)
            }
            Lifecycle.Event.ON_START -> {
                Log.i(TAG, "App in foreground")
                // App in foreground
                AppUtils.setAppOnForeground(true)
            }
            else -> {
                Log.i(TAG, "No state change")
            }
        }
    }
}