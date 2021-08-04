package com.cyberark.identity.provider

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.cyberark.identity.data.network.CyberarkAuthBuilder
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.device.DeviceConstants
import com.cyberark.identity.util.device.DeviceInfoHelper
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import com.cyberark.identity.viewmodel.base.CyberarkViewModelFactory
import org.json.JSONObject

internal class CyberarkEnrollmentManager(
    private val context: Context,
    private val accessToken: String
) {
    private val TAG: String? = CyberarkEnrollmentManager::class.simpleName
    private val viewModel: EnrollmentViewModel

    internal fun enroll() {
        viewModel.handleEnrollment(getHeaderPayload(), getBodyPayload())
    }

    internal val getViewModelInstance: EnrollmentViewModel
        get() = viewModel

    init {
        val appContext: AppCompatActivity = context as AppCompatActivity
        viewModel = ViewModelProviders.of(
            appContext,
            CyberarkViewModelFactory(CyberarkAuthHelper(CyberarkAuthBuilder.cyberarkAuthService))
        ).get(EnrollmentViewModel::class.java)
    }

    private fun getBodyPayload(): JSONObject {
        val deviceInfoHelper = DeviceInfoHelper()
        val payload = JSONObject()
        payload.put(DeviceConstants.KEY_DEVICE_NAME, deviceInfoHelper.getDeviceName())
        payload.put(DeviceConstants.KEY_DEVICE_SIMPLE_NAME, deviceInfoHelper.getDeviceName())
        payload.put(DeviceConstants.KEY_DEVICE_VERSION, deviceInfoHelper.getDeviceVersion())
        payload.put(DeviceConstants.KEY_DEVICE_UDID, deviceInfoHelper.getUDID(context))
        payload.put(DeviceConstants.KEY_DEVICE_MANUFACTURER, deviceInfoHelper.getManufacture())
        payload.put(DeviceConstants.KEY_DEVICE_TYPE, "A")
        payload.put(DeviceConstants.KEY_DEVICE_OS, "Android")
        return payload
    }

    private fun getHeaderPayload(): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_CENTRIFY_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_ACCEPT_LANGUAGE, "en-IN")
        payload.put(EndpointUrls.HEADER_AUTHORIZATION, "Bearer " + accessToken)
        return payload
    }
}