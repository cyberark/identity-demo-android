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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.identity.util.keystore.KeyStoreProvider

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Launch URL in browser, set-up view model, start authentication flow
     * and handle API response using active observer
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    protected fun login(cyberArkAccountBuilder: CyberArkAccountBuilder, progressBar: ProgressBar) {
        val authResponseHandler: LiveData<ResponseHandler<AuthCodeFlowModel>> =
            CyberArkAuthProvider.login(cyberArkAccountBuilder).start(this)

        // Verify if there is any active observer, if not then add observer to get API response
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Show authentication success message using Toast
                        Toast.makeText(
                            this,
                            "Received Access Token & Refresh Token" + ResponseStatus.SUCCESS.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Save access token and refresh token in SharedPref using keystore encryption
                        KeyStoreProvider.get().saveAuthToken(it.data!!.access_token)
                        KeyStoreProvider.get().saveRefreshToken(it.data!!.refresh_token)
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Start MFAActivity
                        val intent = Intent(this, MFAActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    ResponseStatus.ERROR -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Show authentication error message using Toast
                        Toast.makeText(
                            this,
                            "Error: Unable to fetch Access Token & Refresh Token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ResponseStatus.LOADING -> {
                        // Show progress indicator
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }
}