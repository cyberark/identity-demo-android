package com.cyberark.mfa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

    private val tag: String? = MFAActivity::class.simpleName

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    // Refresh token data variable
    private lateinit var refreshTokenData: String

    // Access token data variable
    private lateinit var accessTokenData: String

    // Enroll, QR Authenticator and logout button variables
    private lateinit var enrollButton: Button
    private lateinit var scanQRCodeButton: Button
    private lateinit var logOut: Button

    // Device biometrics checkbox variables
    private lateinit var launchWithBio: CheckBox
    private lateinit var biometricReqOnRefresh: CheckBox

    // SDK biometrics utility class variable
    private lateinit var bioMetric: BiometricPromptUtility

    // Status update flag in order to handle UI and flow
    private var isAuthenticationReq = false
    private var logoutStatus: Boolean = false
    private var isAuthenticated: Boolean = false

    /**
     * Callback to handle QR Authenticator result
     */
    private val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data?.getStringExtra("QR_CODE_AUTH_RESULT")
                    Log.i(tag, "data :: " + data.toString())
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

        // Invoke UI elements
        invokeUI()

        // Perform initialize data
        initializeData()

        // Perform update UI
        updateUI()

        // Perform enroll device
        enrollButton.setOnClickListener {
            handleClick(it)
        }

        // Perform QR Code Authenticator
        scanQRCodeButton.setOnClickListener {
            handleClick(it)
        }

        // Perform logout
        logOut.setOnClickListener {
            handleClick(it)
        }

        // Handle device biometrics
        isAuthenticationReq = CyberarkPreferenceUtils.getBoolean(getString(R.string.isbiometricReq), false)
        launchWithBio.isChecked = isAuthenticationReq
        launchWithBio.setOnClickListener {
            handleClick(it)
        }
        biometricReqOnRefresh.isChecked = CyberarkPreferenceUtils.getBoolean(getString(R.string.refreshBioReq), false)
        biometricReqOnRefresh.setOnClickListener {
            handleClick(it)
        }
        bioMetric = BiometricManager().getBiometricUtility(biometricCallback)
    }

    override fun onResume() {
        super.onResume()
        if (isAuthenticationReq && !isAuthenticated) {
            // Invoke biometrics prompt
            showBiometric()
        } else {
            // Update flag status and handle logout scenario
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

                // Start HomeActivity in logout action success
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Invoke all UI elements
     */
    private fun invokeUI() {
        progressBar = findViewById(R.id.progressBar_mfa_activity)
        scanQRCodeButton = findViewById(R.id.scan_qr_code)
        logOut = findViewById(R.id.button_end_session)
        enrollButton = findViewById(R.id.button_enroll)
        launchWithBio = findViewById(R.id.biometricReq)
        biometricReqOnRefresh = findViewById(R.id.biometricReqOnRefresh)
    }

    /**
     * Get access token and refresh token from device storage using keystore and initialize in variables
     */
    private fun initializeData() {
        accessTokenData = KeyStoreProvider.get().getAuthToken().toString()
        refreshTokenData = KeyStoreProvider.get().getRefreshToken().toString()
    }

    /**
     * Verify and access token status and enrollment status and update UI accordingly
     */
    private fun updateUI() {
        if (::accessTokenData.isInitialized) {
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

    /**
     * Enroll device and receive access token and refresh token
     */
    private fun enroll() {
        val authResponseHandler: LiveData<ResponseHandler<EnrollmentModel>> =
                CyberarkAuthProvider.enroll().start(this, accessTokenData)
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(tag, ResponseStatus.SUCCESS.toString())
                        Log.i(tag, it.data.toString())
                        Log.i(tag, it.data!!.success.toString())
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
                        Log.i(tag, ResponseStatus.ERROR.toString())
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
        Log.i(tag, cyberarkAccountBuilder.OAuthBaseURL)
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
        val refreshTokenResponseHandler: LiveData<ResponseHandler<RefreshTokenModel>> =
                CyberarkAuthProvider.refreshToken(cyberarkAccountBuilder).start(this, refreshTokenData)
        if (!refreshTokenResponseHandler.hasActiveObservers()) {
            refreshTokenResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(tag, ResponseStatus.SUCCESS.toString())
                        Log.i(tag, it.data.toString())
                        Log.i(tag, it.data!!.access_token)

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
                        Log.i(tag, ResponseStatus.ERROR.toString())
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

    /**
     * Handle all click actions
     */
    private fun handleClick(view: View) {
        if (!isAuthenticated && isAuthenticationReq) {
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
        } else if (view.id == R.id.button_end_session) {
            val account = setupAccount()
            endSession(account)
        } else if (view.id == R.id.biometricReqOnRefresh) {
            CyberarkPreferenceUtils.putBoolean(getString(R.string.refreshBioReq), biometricReqOnRefresh.isChecked)
        } else if (view.id == R.id.biometricReq) {
            CyberarkPreferenceUtils.putBoolean(getString(R.string.isbiometricReq), launchWithBio.isChecked)
        }
    }

    /**
     * Show all strong biometrics in a prompt
     */
    private fun showBiometric() {
        bioMetric.showBioAuthentication(this, null, "Use App Pin", false)
    }

    /**
     * Callback to handle biometrics response
     */
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
            if (!boolean) {
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
            if (!boolean) {
                showBiometricsEnrollmentAlert()
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

    /**
     * Show biometrics enrollment popup if not registered
     */
    private fun showBiometricsEnrollmentAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonwithType(buttonType: AlertButtonType) {
                if(buttonType == AlertButtonType.NEGATIVE) {
//                    finish()
                } else if(buttonType == AlertButtonType.POSITIVE) {
                    launchBiometricSetup()
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
                this,
                this.getString(R.string.cyberark),
                this.getString(R.string.biometricDescription), false,
                mutableListOf(AlertButton("Cancel", AlertButtonType.NEGATIVE), AlertButton("OK", AlertButtonType.POSITIVE))
        )
    }

    /**
     * Invoke security settings screen to register biometrics
     */
    private fun launchBiometricSetup() {
        this.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
}