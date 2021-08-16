/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyberark.identity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.*
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.viewmodel.ScanQRCodeViewModel
import com.cyberark.identity.viewmodel.base.CyberarkViewModelFactory
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * Cyberark QR code login activity
 *
 * @constructor Create empty Cyberark q r code login activity
 */
class CyberarkQRCodeLoginActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: ScanQRCodeViewModel
    private lateinit var accessTokenData: String
    private var gotQRResult: Boolean = false

    companion object {
        private const val TAG = "ScanQRCodeLoginActivity"
        private const val REQUEST_CODE_CAMERA_PERMISSION = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        progressBar = findViewById(R.id.progressBar)

        if (intent.extras != null) {
            accessTokenData = intent.getStringExtra("access_token").toString()
        }
        setupViewModel()
        requestCameraPermission()
    }

    /**
     * Has camera permission
     *
     * @return
     */
    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    /**
     * Request camera permission
     *
     */
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

    /**
     * Start QR code scan
     *
     */
    private fun startQRCodeScan() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Place a QR code inside the viewfinder rectangle to scan it")
        integrator.initiateScan()
    }

    /**
     * Setup view model
     *
     */
    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            CyberarkViewModelFactory(CyberArkAuthHelper(CyberArkAuthBuilder.CYBER_ARK_AUTH_SERVICE))
        ).get(ScanQRCodeViewModel::class.java)
    }

    /**
     * Setup observer
     *
     */
    private fun setupObserver() {
        viewModel.qrCodeLogin().observe(this, {
            val intent = Intent()
            when (it.status) {
                ResponseStatus.SUCCESS -> {

                    //TODO.. for testing only added logs and should be removed
                    Log.i(TAG, ResponseStatus.SUCCESS.toString())
                    Log.i(TAG, it.data.toString())
                    Log.i(TAG, it.data!!.success.toString())
                    Log.i(TAG, it.data.result?.displayName.toString())

                    progressBar.visibility = View.GONE

                    intent.putExtra(
                        "QR_CODE_AUTH_RESULT",
                        "QR Code Authentication is done successfully"
                    )
                    setResult(RESULT_OK, intent)
                    finish()
                }
                ResponseStatus.ERROR -> {
                    Log.i(TAG, ResponseStatus.ERROR.toString())

                    progressBar.visibility = View.GONE

                    intent.putExtra(
                        "QR_CODE_AUTH_RESULT",
                        "QR Code Authentication is failed"
                    )
                    setResult(RESULT_OK, intent)
                    finish()
                }
                ResponseStatus.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                this,
                R.string.toast_returned_from_app_settings_to_activity,
                Toast.LENGTH_SHORT
            ).show()
            finish()
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

    /**
     * Get header payload
     *
     * @return
     */
    private fun getHeaderPayload(): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_AUTHORIZATION, "Bearer $accessTokenData")
        return payload
    }
}