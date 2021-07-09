package com.cyberark.mfa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.cyberark.identity.ScanQRCodeLoginActivity
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.provider.CyberarkAuthProvider
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus

class MainActivity : AppCompatActivity() {

    private val TAG: String? = MainActivity::class.simpleName

    // OAuth access token handler
    private lateinit var authResponseHandler: LiveData<ResponseHandler<AuthCodeFlowModel>>

    // OAuth refresh token handler
    private lateinit var refreshTokenResponseHandler: LiveData<ResponseHandler<RefreshTokenModel>>

    // Refresh token data variable
    private lateinit var refreshTokenData: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // OAuth Authorization Code Flow + PKCE
        val account = setupAccount()
        val buttonLogin: Button = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener {
            startAuthentication(account)
        }
        val buttonEndSession: Button = findViewById(R.id.button_end_session)
        buttonEndSession.setOnClickListener {
            endSession(account)
        }
        val buttonRefreshToken: Button = findViewById(R.id.button_refresh_token)
        buttonRefreshToken.setOnClickListener {
            getAccessTokenUsingRefreshToken(account)
        }

        // QR Code Authenticator Flow
        val scanQRCodeButton: Button = findViewById(R.id.scan_qr_code)
        scanQRCodeButton.setOnClickListener {
            val intent = Intent(this, ScanQRCodeLoginActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Set-up account for OAuth 2.0 PKCE driven flow
     */
    private fun setupAccount(): CyberarkAccountBuilder {
        val cyberarkAccountBuilder = CyberarkAccountBuilder.Builder()
                .clientId(getString(R.string.cyberark_account_client_id))
                .domainURL(getString(R.string.cyberark_account_host))
                .appId(getString(R.string.cyberark_account_app_id))
                .responseType(getString(R.string.cyberark_account_response_type))
                .scope(getString(R.string.cyberark_account_scope))
                .redirectUri(getString(R.string.cyberark_account_redirect_uri))
                .build()
        Log.i(TAG, cyberarkAccountBuilder.OAuthBaseURL)
        return cyberarkAccountBuilder
    }

    /**
     * Launch URL in browser, set-up view model and start authentication flow
     */
    private fun startAuthentication(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        authResponseHandler = CyberarkAuthProvider.login(cyberarkAccountBuilder).start(this)
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        Log.i(TAG, ResponseStatus.SUCCESS.toString())
                        Log.i(TAG, it.data.toString())
                        Log.i(TAG, it.data!!.access_token)
                        Log.i(TAG, it.data!!.refresh_token)
                        refreshTokenData = it.data!!.refresh_token
                        Toast.makeText(
                                this,
                                "Received Access Token & Refresh Token",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    ResponseStatus.ERROR -> {
                        Log.i(TAG, ResponseStatus.ERROR.toString())
                    }
                }
            })
        }
    }

    /**
     * End session from custom tab browser
     */
    private fun endSession(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        CyberarkAuthProvider.endSession(cyberarkAccountBuilder).start(this)
    }

    /**
     * Get the access token using refresh token
     */
    private fun getAccessTokenUsingRefreshToken(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        refreshTokenResponseHandler = CyberarkAuthProvider.refreshToken(cyberarkAccountBuilder).start(this, refreshTokenData)
        if (!refreshTokenResponseHandler.hasActiveObservers()) {
            refreshTokenResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        Log.i(TAG, ResponseStatus.SUCCESS.toString())
                        Log.i(TAG, it.data.toString())
                        Log.i(TAG, it.data!!.access_token)
                        Toast.makeText(
                                this,
                                "Received New Access Token",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    ResponseStatus.ERROR -> {
                        Log.i(TAG, ResponseStatus.ERROR.toString())
                    }
                }
            })
        }
    }

}