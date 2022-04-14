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

package com.cyberark.mfa.activity.scenario2

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.activity.view.CyberArkMFAWidgetFragment
import com.cyberark.identity.util.widget.WidgetConstants
import com.cyberark.mfa.activity.common.PopupActivity
import com.cyberark.mfa.R

class MFAWidgetActivity : AppCompatActivity(), CyberArkMFAWidgetFragment.LoginSuccessListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mfa_widget)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.acme)

        if (intent.extras != null) {
            val widgetURL = intent.getStringExtra("MFA_WIDGET_URL").toString()
            val args = Bundle()
            args.putString(WidgetConstants.WIDGET_URL, widgetURL)
            val mfaWidgetFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container_view) as CyberArkMFAWidgetFragment
            mfaWidgetFragment.arguments = args
        }
    }

    override fun onLoginSuccess() {
        // Finish MFAWidgetActivity
        val intent = Intent(this, PopupActivity::class.java)
        intent.putExtra("title", getString(R.string.transfer_fund_login_success_title))
        intent.putExtra("desc", getString(R.string.transfer_fund_login_success_desc))
        startActivity(intent)
        finish()
    }
}