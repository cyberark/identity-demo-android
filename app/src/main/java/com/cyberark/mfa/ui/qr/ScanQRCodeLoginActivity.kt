package com.cyberark.mfa.ui.qr

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
import com.cyberark.mfa.R
import com.cyberark.mfa.ui.base.ViewModelFactory
import com.google.zxing.integration.android.IntentIntegrator
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

private const val TAG = "ScanQRCodeLoginActivity"
private const val REQUEST_CODE_CAMERA_PERMISSION = 123

class ScanQRCodeLoginActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var viewModel: ScanQRCodeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        requestCameraPermission()
    }

    private fun hasCameraPermission():Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    @AfterPermissionGranted(REQUEST_CODE_CAMERA_PERMISSION)
    private fun requestCameraPermission() {
        if (hasCameraPermission()) {
            // Have permission, do things!
                startQRCodeScan()
            Toast.makeText(this, "TODO: Camera things", Toast.LENGTH_LONG).show()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
            ViewModelFactory(CyberarkAuthHelper(CyberarkAuthBuilder.cyberarkAuthService))
        ).get(ScanQRCodeViewModel::class.java)
    }

    private fun setupObserver() {
        viewModel.qrCodeLogin().observe(this, {
            when (it.status) {
                ResponseStatus.SUCCESS -> {
                }
                ResponseStatus.LOADING -> {
                }
                ResponseStatus.ERROR -> {
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT).show();
            finish()
        } else {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
            finish()
        }
    }
}
