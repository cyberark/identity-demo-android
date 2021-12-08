package com.cyberark.mfa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_welcome)
        setupHyperlink()
    }

    private fun setupHyperlink() {
        val linkTextView: TextView = findViewById(R.id.api_doc)
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}