package com.cyberark.mfa

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.cyberark.mfa.utils.AppConfig

class LoginOptionsActivity : HomeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_options)
        val account =  AppConfig.setupAccountFromSharedPreference(this)
        findViewById<CardView>(R.id.cv_redirect_login).setOnClickListener {
            login(account)
        }
        inflateMenuFromToolbar()
    }

    // **************** Handle menu settings click action Start *********************** //

    private fun inflateMenuFromToolbar() {
        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.title = "Acme"
        }
        toolbar.inflateMenu(R.menu.settings_menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            //Start Settings activity
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("from_activity", "HomeActivity")
            startActivity(intent)
            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun showInfo(view: android.view.View) {
        val intent = Intent(this,AlertActivity::class.java)
        intent.putExtra("info",getString(R.string.login_hosted_description))
        startActivity(intent)
    }
    // **************** Handle menu settings click action End *********************** //
}