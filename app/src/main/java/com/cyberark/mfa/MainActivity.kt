package com.cyberark.mfa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.cyberark.identity.ScanQRCodeLoginActivity
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.provider.CyberarkAuthProvider
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus

class MainActivity : AppCompatActivity() {

    private val TAG: String? = MainActivity::class.simpleName

    // Refresh token data variable
    private lateinit var refreshTokenData: String

    // Access token data variable
    private lateinit var accessTokenData: String

    private lateinit var scanQRCodeButton: Button
    private lateinit var signInButton: Button
    private lateinit var logOut: Button
    private lateinit var refreshToken: Button
    private lateinit var enrollButton: Button

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                var data = result.data?.getStringExtra("QR_CODE_AUTH_RESULT")
                Log.i(TAG, "data :: " + data.toString())
                Toast.makeText(
                    this,
                    data.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanQRCodeButton = findViewById(R.id.scan_qr_code)
        signInButton = findViewById(R.id.button_login)
        logOut = findViewById(R.id.button_end_session)
        refreshToken = findViewById(R.id.button_refresh_token)
        enrollButton = findViewById(R.id.button_enroll)

        // OAuth Authorization Code Flow + PKCE
        val account = setupAccount()
        signInButton.setOnClickListener {
            startAuthentication(account)
        }
        logOut.setOnClickListener {
            endSession(account)
        }
        refreshToken.setOnClickListener {
            getAccessTokenUsingRefreshToken(account)
        }

        // QR Code Authenticator Flow
        scanQRCodeButton.setOnClickListener {
            val intent = Intent(this, ScanQRCodeLoginActivity::class.java)
            if (::accessTokenData.isInitialized) {
                intent.putExtra("access_token", accessTokenData)
                startForResult.launch(intent)
            } else {
                //TODO.. handle error scenario
            }
        }

        //Enroll device
        enrollButton.setOnClickListener {
            if (::accessTokenData.isInitialized) {
                enroll()
            } else {
                //TODO.. handle error scenario
            }
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
        var authResponseHandler: LiveData<ResponseHandler<AuthCodeFlowModel>> =
            CyberarkAuthProvider.login(cyberarkAccountBuilder).start(this)
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(TAG, ResponseStatus.SUCCESS.toString())
                        Log.i(TAG, it.data.toString())
                        Log.i(TAG, it.data!!.access_token)
                        Log.i(TAG, it.data!!.refresh_token)

                        refreshTokenData = it.data!!.refresh_token
                        accessTokenData = it.data!!.access_token
                        Toast.makeText(
                            this,
                            "Received Access Token & Refresh Token",
                            Toast.LENGTH_SHORT
                        ).show()

                        //Update button enable/disable status
                        signInButton.isEnabled = false
                        logOut.isEnabled = true
                        refreshToken.isEnabled = true
                        scanQRCodeButton.isEnabled = false
                        enrollButton.isEnabled = true
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
        //Update button enable/disable status
        signInButton.isEnabled = true
        logOut.isEnabled = false
        refreshToken.isEnabled = false
        scanQRCodeButton.isEnabled = false
        enrollButton.isEnabled = false
    }

    /**
     * Get the access token using refresh token
     */
    private fun getAccessTokenUsingRefreshToken(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        var refreshTokenResponseHandler: LiveData<ResponseHandler<RefreshTokenModel>> =
            CyberarkAuthProvider.refreshToken(cyberarkAccountBuilder).start(this, refreshTokenData)
        if (!refreshTokenResponseHandler.hasActiveObservers()) {
            refreshTokenResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(TAG, ResponseStatus.SUCCESS.toString())
                        Log.i(TAG, it.data.toString())
                        Log.i(TAG, it.data!!.access_token)

                        accessTokenData = it.data!!.access_token
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

    /**
     * Enroll device
     */
    private fun enroll() {
        var authResponseHandler: LiveData<ResponseHandler<EnrollmentModel>> =
            CyberarkAuthProvider.enroll().start(this, accessTokenData)
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(TAG, ResponseStatus.SUCCESS.toString())
                        Log.i(TAG, it.data.toString())
                        Log.i(TAG, it.data!!.success.toString())
                        scanQRCodeButton.isEnabled = true
                        enrollButton.isEnabled = false

                        Toast.makeText(
                            this,
                            "Enrolled successfully",
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