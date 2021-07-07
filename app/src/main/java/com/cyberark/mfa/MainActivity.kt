package com.cyberark.mfa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.provider.CyberarkAuthProvider
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.identity.ScanQRCodeLoginActivity
//import com.cyberark.mfa.ui.qr.ScanQRCodeLoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanQRCodeButton: Button = findViewById(R.id.scan_qr_code)

        scanQRCodeButton.setOnClickListener {
            val intent = Intent(this, ScanQRCodeLoginActivity::class.java)
            startActivity(intent)
        }

        val buttonLogin: Button = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener {

            val account = setupAccount()
            startAuthentication(account)
            addObserver()
        }
    }

    private fun setupAccount(): CyberarkAccountBuilder {
        val cyberarkAccountBuilder = CyberarkAccountBuilder.Builder()
                .clientId(getString(R.string.cyberark_account_client_id))
                .domainURL(getString(R.string.cyberark_account_host))
                .appId(getString(R.string.cyberark_account_app_id))
                .responseType(getString(R.string.cyberark_account_response_type))
                .scope(getString(R.string.cyberark_account_scope))
                .redirectUri(getString(R.string.cyberark_account_redirect_uri))
                .build()
        Log.i("cyberarkAccountBuilder ", cyberarkAccountBuilder.OAuthBaseURL)
        return cyberarkAccountBuilder
    }

    private fun startAuthentication(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        CyberarkAuthProvider
                .login(this, cyberarkAccountBuilder)
                .setupViewModel()
                .start()
    }

    private fun addObserver() {
        CyberarkAuthProvider.getAuthViewModel()
                .getAccessToken()
                .observe(this, {
            when (it.status) {
                ResponseStatus.SUCCESS -> {
                    Log.i("MainActivity SUCCESS", ResponseStatus.SUCCESS.toString())
                    Log.i("MainActivity data", it.data.toString())
                }
                ResponseStatus.ERROR -> {
                    Log.i("MainActivity Error", ResponseStatus.ERROR.toString())
                }
            }
        })
    }
}