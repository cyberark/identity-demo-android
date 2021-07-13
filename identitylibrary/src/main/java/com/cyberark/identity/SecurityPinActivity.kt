package com.cyberark.identity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SecurityPinActivity : AppCompatActivity(),View.OnClickListener {

    private val TAG = "SecurityPinActivity"
    private var authecticateBtn:Button? = null
    private var messageText:TextView? = null
    private var pinField:EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(android.R.style.Theme_Dialog)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_pin)
        setUPUI()
    }

    private fun setUPUI() {
        authecticateBtn = findViewById(R.id.authenticate)
        messageText = findViewById(R.id.messageText)
        pinField = findViewById(R.id.pinField)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            authecticateBtn?.setTextColor(getResources().getColor(android.R.color.white,null))
            messageText?.setTextColor(getResources().getColor(android.R.color.black,null))
        }else {
            authecticateBtn?.setTextColor(getResources().getColor(android.R.color.white))
            messageText?.setTextColor(getResources().getColor(android.R.color.black))
        }

        pinField?.setHint("Enter PIN here")
        messageText?.setText("Please enter PIN to authenticate")
        authecticateBtn?.setOnClickListener(this)
        authecticateBtn?.text = "Authenticate"
    }

    override fun onClick(view: View?) {
        if (authecticateBtn?.id != null && view?.id == authecticateBtn?.id) {
            //Authentiate tapped
            if (pinField!!.text.toString() == intent.getStringExtra("securitypin")) {
                setResult(RESULT_OK, Intent().putExtra("RESULT","Success"))
                finish()
            } else {
                //Handle pin didn't match scenario
            }
        }
    }
}