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
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize CyberArk Preference Util
        CyberArkPreferenceUtil.init(this)
    }
}