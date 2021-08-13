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

package com.cyberark.identity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class CyberarkRedirectActivity : AppCompatActivity() {

    private val TAG: String? = CyberarkRedirectActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cyberarkAuthActivityIntent = Intent(this, CyberarkAuthActivity::class.java)
        cyberarkAuthActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (intent != null) {
            cyberarkAuthActivityIntent.data = intent.data
            //TODO.. for testing only added this log and should be removed later
            Log.i(TAG, cyberarkAuthActivityIntent.data.toString())
        }
        startActivity(cyberarkAuthActivityIntent)
        finish()
    }
}