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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.cyberark.identity.provider.CyberArkAuthProvider.getAuthorizeToken
import com.cyberark.identity.util.browser.CustomTabHelper
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil

/**
 * Cyberark auth activity
 *
 * @constructor Create empty Cyberark auth activity
 */
class CyberarkAuthActivity : AppCompatActivity() {

    private val TAG: String? = CyberarkAuthActivity::class.simpleName

    private var activityLaunched = false
    private var customTabHelper: CustomTabHelper = CustomTabHelper()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CyberArkPreferenceUtil.init(this)
        if (savedInstanceState != null) {
            activityLaunched = savedInstanceState.getBoolean(ACTIVITY_LAUNCHED, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean(ACTIVITY_LAUNCHED, activityLaunched)
    }

    override fun onResume() {
        super.onResume()
        val authData = intent
        if (!activityLaunched && authData.extras == null) {
            Log.i(TAG, "Activity intent doesn't hold the authorize uri to launch in browser")
            finish()
            return
        } else if (!activityLaunched) {
            Log.i(TAG, "launch uri in browser successfully")
            activityLaunched = true
            val extras = intent.extras
            val authorizeUri = extras!!.getParcelable<Uri>(AUTHORIZE_URI)
            launchUri(this, authorizeUri!!)
            return
        }
        Log.i(TAG, "Get authorize token")
        getAuthorizeToken(authData)
        finish()
    }

    internal companion object {
        private const val AUTHORIZE_URI = "com.cyberark.identity.AUTHORIZE_URI"
        private const val ACTIVITY_LAUNCHED = "com.cyberark.identity.ACTIVITY_LAUNCHED"

        @JvmStatic
        internal fun authenticateUsingCustomTab(
                context: Context,
                authorizeUri: Uri
        ) {
            val intent = Intent(context, CyberarkAuthActivity::class.java)
            intent.putExtra(AUTHORIZE_URI, authorizeUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }

    /**
     * Launch uri
     *
     * @param context
     * @param uri
     */
    fun launchUri(context: Context, uri: Uri?) {
        val builder = CustomTabsIntent.Builder()

        // show website title
        builder.setShowTitle(true)

        // animation for enter and exit of tab
        builder.setStartAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
        builder.setExitAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)

        val customTabsIntent = builder.build()

        // check is chrome available
        val packageName = customTabHelper.getPackageName(context, uri.toString())

        if (packageName == null) {
            Log.i(TAG, "Chrome custom tab is not available")
            // if chrome not available open in web view
            //TODO.. handle error scenario
        } else {
            Log.i(TAG, "Chrome custom tab is available")
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(context, uri!!)
        }
    }
}