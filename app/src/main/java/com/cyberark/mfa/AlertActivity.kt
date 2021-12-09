package com.cyberark.mfa

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView

class AlertActivity : Activity() {

    var contentText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alert)
        contentText = findViewById(R.id.contentText)
        contentText?.setText(intent.extras?.getString("info"))
    }
}