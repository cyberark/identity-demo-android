package com.cyberark.mfa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.cyberark.identity.ScanQRCodeLoginActivity
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.provider.CyberarkAuthProvider
import com.cyberark.identity.util.*
import com.cyberark.identity.util.biometric.BiometricAuthenticationCallback
import com.cyberark.identity.util.biometric.BiometricManager
import com.cyberark.identity.util.biometric.BiometricPromptUtility
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberarkPreferenceUtils

class MFAActivity : AppCompatActivity() {

    private val TAG: String? = MFAActivity::class.simpleName
    private lateinit var progressBar: ProgressBar

    // Refresh token data variable
    private lateinit var refreshTokenData: String

    // Access token data variable
    private lateinit var accessTokenData: String

    private lateinit var scanQRCodeButton: Button
    private lateinit var logOut: Button
    private lateinit var enrollButton: Button
    private lateinit var launchWithBio:CheckBox
    private lateinit var biometricReqOnRefresh:CheckBox
    private var isAuthenticationReq = false
    private var logoutStatus: Boolean = false

    private var isAuthenticated: Boolean = false
    private lateinit var bioMetric: BiometricPromptUtility

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
        setContentView(R.layout.activity_mfa)

        progressBar = findViewById(R.id.progressBar_mfa_activity)

        scanQRCodeButton = findViewById(R.id.scan_qr_code)
        logOut = findViewById(R.id.button_end_session)
        enrollButton = findViewById(R.id.button_enroll)
        launchWithBio = findViewById(R.id.biometricReq)
        biometricReqOnRefresh = findViewById(R.id.biometricReqOnRefresh)
        // Verify access token and update UI
        updateUI()

        // Initialize access token and refresh token form keystore
        accessTokenData = KeyStoreProvider.get().getAuthToken().toString()
        refreshTokenData = KeyStoreProvider.get().getRefreshToken().toString()

        //TODO.. need to be removed later
//        if (intent.extras != null) {
//            accessTokenData = intent.getStringExtra("access_token").toString()
//            refreshTokenData = intent.getStringExtra("refresh_token").toString()
//        }

        //Enroll device
        enrollButton.setOnClickListener {
            handleClick(it)
        }

        // QR Code Authenticator Flow
        scanQRCodeButton.setOnClickListener {
            handleClick(it)
        }
        isAuthenticationReq = CyberarkPreferenceUtils.getBoolean(getString(R.string.isbiometricReq),false)
        launchWithBio.isChecked = isAuthenticationReq
        launchWithBio.setOnClickListener({
            handleClick(it)
        })

        biometricReqOnRefresh.isChecked = CyberarkPreferenceUtils.getBoolean(getString(R.string.refreshBioReq),false)
        biometricReqOnRefresh.setOnClickListener({
            handleClick(it)
        })

        // OAuth Authorization Code Flow + PKCE
        logOut.setOnClickListener {
            handleClick(it)
        }
        bioMetric = BiometricManager().getBiometricUtility(biometricCallback)

    }

    override fun onResume() {
        super.onResume()
        if (isAuthenticationReq == true && isAuthenticated == false) {
            showBiometric()
        }else {
            biometricReqOnRefresh.isEnabled = true
            launchWithBio.isEnabled = true
            if (logoutStatus) {
                // Clear storage on logout
                CyberarkPreferenceUtils.remove(Constants.AUTH_TOKEN)
                CyberarkPreferenceUtils.remove(Constants.AUTH_TOKEN_IV)
                CyberarkPreferenceUtils.remove(Constants.REFRESH_TOKEN)
                CyberarkPreferenceUtils.remove(Constants.REFRESH_TOKEN_IV)
                CyberarkPreferenceUtils.remove("ENROLLMENT_STATUS")
                CyberarkPreferenceUtils.clear()
                val intent = Intent(this@MFAActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showBiometric() {
        bioMetric.showBioAuthentication(this, null, "Use App Pin", false)
    }

    private fun handleClick(view: View) {
        if (isAuthenticated == false && isAuthenticationReq == true) {
            showBiometric()
            return
        }
        if (view.id == R.id.button_enroll) {
            if (::accessTokenData.isInitialized) {
                enroll()
            } else {
                //TODO.. handle error scenario
            }
        } else if (view.id == R.id.scan_qr_code) {
            val intent = Intent(this, ScanQRCodeLoginActivity::class.java)
            if (::accessTokenData.isInitialized) {
                intent.putExtra("access_token", accessTokenData)
                startForResult.launch(intent)
            } else {
                //TODO.. handle error scenario
            }
        } else if (view.id == R.id.button_end_session){
            val account = setupAccount()
            endSession(account)
        } else if(view.id == R.id.biometricReqOnRefresh) {
            CyberarkPreferenceUtils.putBoolean(getString(R.string.refreshBioReq),biometricReqOnRefresh.isChecked)
        }  else if(view.id == R.id.biometricReq) {
            CyberarkPreferenceUtils.putBoolean(getString(R.string.isbiometricReq),launchWithBio.isChecked)
        }
    }

    private val biometricCallback = object : BiometricAuthenticationCallback {
        override fun isAuthenticationSuccess(success: Boolean) {
            Toast.makeText(
                this@MFAActivity,
                "Authentication success",
                Toast.LENGTH_LONG
            ).show()
            this@MFAActivity.isAuthenticated = true
            biometricReqOnRefresh.isEnabled = true
            launchWithBio.isEnabled = true
        }

        override fun passwordAuthenticationSelected() {
            Toast.makeText(
                this@MFAActivity,
                "Password authentication selected",
                Toast.LENGTH_LONG
            ).show()
//            val pinIntent = Intent(
//                this@MFAActivity,
//                SecurityPinActivity::class.java
//            ).apply {
//                putExtra("securitypin", "1234")
//            }
//            //TODO.. need to verify deprecation warning and refactor code as needed
//            startActivityForResult(pinIntent, APP_PIN_REQUEST)
        }

        override fun showErrorMessage(message: String) {
            Toast.makeText(this@MFAActivity, message, Toast.LENGTH_LONG).show()
        }

        override fun isHardwareSupported(boolean: Boolean) {
            if (boolean == false) {
                Toast.makeText(
                    this@MFAActivity,
                    "Hardware not supported",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun isSdkVersionSupported(boolean: Boolean) {
            Toast.makeText(
                this@MFAActivity,
                "SDK version not supported",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun isBiometricEnrolled(boolean: Boolean) {
            if (boolean == false) {
                showFingerEnrollmentAlert()
            }
        }

        override fun biometricErrorSecurityUpdateRequired() {
            Toast.makeText(
                this@MFAActivity,
                "Biometric security updates required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateUI() {
        var accessToken = KeyStoreProvider.get().getAuthToken()
        if(accessToken != null) {
            logOut.isEnabled = true
            if (CyberarkPreferenceUtils.getBoolean("ENROLLMENT_STATUS", false)) {
                scanQRCodeButton.isEnabled = true
                enrollButton.isEnabled = false
            } else {
                scanQRCodeButton.isEnabled = false
                enrollButton.isEnabled = true
            }
        }
    }

    private fun showFingerEnrollmentAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonwithType(buttonType: AlertButtonType) {
                launchBiometricSetup()
            }
        })
        enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.cyberark),
            this.getString(R.string.biometricDescription), false,
            mutableListOf<AlertButton>(AlertButton("OK", AlertButtonType.POSITIVE))
        )
    }

    private fun launchBiometricSetup() {
        this.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
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
                        logOut.isEnabled = true
                        scanQRCodeButton.isEnabled = true
                        enrollButton.isEnabled = false

                        Toast.makeText(
                            this,
                            "Enrolled successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        CyberarkPreferenceUtils.putBoolean("ENROLLMENT_STATUS", true)
                        progressBar.visibility = View.GONE
                    }
                    ResponseStatus.ERROR -> {
                        Log.i(TAG, ResponseStatus.ERROR.toString())
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Error: Unable to Enroll device",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ResponseStatus.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
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
     * End session from custom tab browser
     */
    private fun endSession(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        logoutStatus = true
        CyberarkAuthProvider.endSession(cyberarkAccountBuilder).start(this)
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

                        //Save access token in keystore
                        KeyStoreProvider.get().saveAuthToken(accessTokenData)

                        Toast.makeText(
                            this,
                            "Received New Access Token",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressBar.visibility = View.GONE
                    }
                    ResponseStatus.ERROR -> {
                        Log.i(TAG, ResponseStatus.ERROR.toString())
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Error: Unable to fetch access token using refresh token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ResponseStatus.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }
}