package com.cyberark.identity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.cyberark.identity.data.network.CyberarkAuthBuilder
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.identity.util.biometric.BiometricAuthenticationCallback
import com.cyberark.identity.util.biometric.BiometricPromptUtility
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.viewmodel.ScanQRCodeViewModel
import com.cyberark.identity.viewmodel.base.CyberarkViewModelFactory
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class ScanQRCodeLoginActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var viewModel: ScanQRCodeViewModel
    private var isAuthenticated: Boolean = false
    private lateinit var bioMetric: BiometricPromptUtility
    private lateinit var accessTokenData: String
    private var gotQRResult: Boolean = false

    companion object {
        private const val TAG = "ScanQRCodeLoginActivity"
        private const val REQUEST_CODE_CAMERA_PERMISSION = 123
        private const val APP_PIN_REQUEST = 124
    }

    init {
        registerForBiometricCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        if (intent.extras != null) {
            accessTokenData = intent.getStringExtra("access_token").toString()
        }
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        //TODO.. As this block of code is creating an issue for closing current activity, hence Pavan will verify and update
        if (gotQRResult == false && isAuthenticated) {
            requestCameraPermission()
        } else if (gotQRResult == false){
            //do biometric authentication
            //TODO.. remove hardcoded string value
            bioMetric.showBioAuthentication(this, null, "Use App Pin", false)
        }
//        Dispatchers.Main
    }

    override fun onPause() {
        super.onPause()
        if (::bioMetric.isInitialized) {
            bioMetric?.dismissFingerPrintEnroll()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    // TODO.. remove all hardcoded strings
    private fun registerForBiometricCallback() {
        bioMetric = BiometricPromptUtility(object : BiometricAuthenticationCallback {

            override fun isAuthenticationSuccess(success: Boolean) {
                Toast.makeText(
                    this@ScanQRCodeLoginActivity,
                    "Authentication success",
                    Toast.LENGTH_LONG
                ).show()
                this@ScanQRCodeLoginActivity.isAuthenticated = true
                val authToken = KeyStoreProvider.get().getAuthToken()
                val refreshToken = KeyStoreProvider.get().getRefreshToken()
                requestCameraPermission()
            }

            override fun passwordAuthenticationSelected() {
                Toast.makeText(
                    this@ScanQRCodeLoginActivity,
                    "Password authentication selected",
                    Toast.LENGTH_LONG
                ).show()
                val pinIntent = Intent(
                    this@ScanQRCodeLoginActivity,
                    SecurityPinActivity::class.java
                ).apply {
                    putExtra("securitypin", "1234")
                }
                //TODO.. need to verify deprecation warning and refactor code as needed
                startActivityForResult(pinIntent, APP_PIN_REQUEST)
            }

            override fun showErrorMessage(message: String) {
                Toast.makeText(this@ScanQRCodeLoginActivity, message, Toast.LENGTH_LONG).show()
            }

            override fun isHardwareSupported(boolean: Boolean) {
                if (boolean == false) {
                    Toast.makeText(
                        this@ScanQRCodeLoginActivity,
                        "Hardware not supported",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun isSdkVersionSupported(boolean: Boolean) {
                Toast.makeText(
                    this@ScanQRCodeLoginActivity,
                    "SDK version not supported",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun isBiometricEnrolled(boolean: Boolean) {
                if (boolean == false) {
                    Toast.makeText(
                        this@ScanQRCodeLoginActivity,
                        "Biometric not enabled",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun biometricErrorSecurityUpdateRequired() {
                Toast.makeText(
                    this@ScanQRCodeLoginActivity,
                    "Biometric security updates required",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    @AfterPermissionGranted(REQUEST_CODE_CAMERA_PERMISSION)
    private fun requestCameraPermission() {
        if (hasCameraPermission()) {
            // Have permission, do things!
            startQRCodeScan()
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_camera_rationale_message),
                REQUEST_CODE_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            finish()
        }
    }

    private fun startQRCodeScan() {
        val integrator = IntentIntegrator(this)
        integrator.initiateScan()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            CyberarkViewModelFactory(CyberarkAuthHelper(CyberarkAuthBuilder.cyberarkAuthService))
        ).get(ScanQRCodeViewModel::class.java)
    }

    private fun setupObserver() {
        viewModel.qrCodeLogin().observe(this, {
            val intent = Intent()
            when (it.status) {
                ResponseStatus.SUCCESS -> {

                    //TODO.. for testing only added logs and should be removed
                    Log.i(TAG, ResponseStatus.SUCCESS.toString())
                    Log.i(TAG, it.data.toString())
                    Log.i(TAG, it.data!!.success.toString())
                    Log.i(TAG, it.data!!.result?.displayName.toString())

                    intent.putExtra(
                        "QR_CODE_AUTH_RESULT",
                        "QR Code Authentication is done successfully"
                    )
                    setResult(RESULT_OK, intent)
                    finish()
                }
                ResponseStatus.ERROR -> {
                    Log.i(TAG, ResponseStatus.ERROR.toString())
                    intent.putExtra(
                        "QR_CODE_AUTH_RESULT",
                        "QR Code Authentication is failed"
                    )
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                this,
                R.string.returned_from_app_settings_to_activity,
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else if (requestCode == APP_PIN_REQUEST && resultCode == RESULT_OK) {
            isAuthenticated = true
        } else {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    gotQRResult = true
                    if (::accessTokenData.isInitialized) {
                        viewModel.handleQRCodeResult(getHeaderPayload(), result.contents.toString())
                        setupObserver()
                    } else {
                        //TODO.. handle error scenario
                    }
                }
            } else {
                //TODO.. need to verify deprecation warning and refactor code as needed
                super.onActivityResult(requestCode, resultCode, data)
            }
            //TODO.. temporarily handled for only QRCode and finish() will be called from the observer method, need to check and remove finish() call here
//            finish()
        }
    }

    private fun getHeaderPayload(): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_AUTHORIZATION, "Bearer $accessTokenData")
        return payload
    }
}