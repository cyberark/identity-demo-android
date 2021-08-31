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
import androidx.appcompat.app.AppCompatActivity

/**
 * CyberArkRedirectActivity is used to handle chrome custom tab browser callback
 * once the authentication done successfully
 *
 */
class CyberArkRedirectActivity : AppCompatActivity() {

    companion object {
        val CYBERARK_REDIRECT_ACTION =
            CyberArkRedirectActivity::class.java.canonicalName + ".CYBERARK_REDIRECT_ACTION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cyberArkAuthActivityIntent = Intent(this, CyberArkAuthActivity::class.java)
        cyberArkAuthActivityIntent.action = CYBERARK_REDIRECT_ACTION
        cyberArkAuthActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (intent != null) {
            cyberArkAuthActivityIntent.data = intent.data
        }
        // Get the flags
        val flags: Int = intent.flags
        // Check that the nested intent does not grant URI permissions
        if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0 &&
            flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION == 0
        ) {
            startActivity(cyberArkAuthActivityIntent)
        }
        finish()
    }
}