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