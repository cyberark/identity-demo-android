package com.cyberark.mfa

import android.os.Bundle
import androidx.cardview.widget.CardView
import com.cyberark.mfa.utils.AppConfig

class LoginOptionsActivity : HomeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_options)
        val account =  AppConfig.setupAccountFromSharedPreference(this)
        findViewById<CardView>(R.id.loginexternal).setOnClickListener {
            login(account)
        }
    }
}