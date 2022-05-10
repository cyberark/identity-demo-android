/*
 * Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.
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

package com.cyberark.mfa.activity.scenario1

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.data.model.UserInfoModel
import com.cyberark.mfa.R
import com.google.gson.Gson

class UserInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.user_info)

        val data = intent.extras?.getString("USER_INFO")
        if(data != null) {
            updateUI(data)
        }
        invalidateOptionsMenu()
    }

    /**
     * Update user info details
     *
     * @param data: UserInfoModel
     */
    private fun updateUI(data: String) {
        val userInfoModel = Gson().fromJson(data, UserInfoModel::class.java)

        val authTime: TextView? = findViewById(R.id.auth_time)
        authTime?.text = userInfoModel.auth_time

        val givenName: TextView? = findViewById(R.id.given_name)
        givenName?.text = userInfoModel.given_name

        val name: TextView? = findViewById(R.id.name)
        name?.text = userInfoModel.name

        val emailAddress: TextView? = findViewById(R.id.email)
        emailAddress?.text = userInfoModel.email

        val familyName: TextView? = findViewById(R.id.family_name)
        familyName?.text = userInfoModel.family_name

        val preferredUserName: TextView? = findViewById(R.id.preferred_username)
        preferredUserName?.text = userInfoModel.preferred_username

        val uniqueName: TextView? = findViewById(R.id.unique_name)
        uniqueName?.text = userInfoModel.unique_name

        val emailVerified: TextView? = findViewById(R.id.email_verified)
        emailVerified?.text = userInfoModel.email_verified.toString()
    }

    // **************** Handle menu settings click action Start *********************** //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.save_settings_options, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_settings)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    // **************** Handle menu settings click action End *********************** //

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}