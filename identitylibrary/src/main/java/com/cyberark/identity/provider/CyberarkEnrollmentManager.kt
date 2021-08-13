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

/**
 * Cyberark enrollment manager
 *
 * @property context
 * @property accessToken
 * @constructor Create empty Cyberark enrollment manager
 */
internal class CyberarkEnrollmentManager(
    private val context: Context,
    private val accessToken: String
) {
    private val tag: String? = CyberarkEnrollmentManager::class.simpleName
    private val viewModel: EnrollmentViewModel

    /**
     * Enroll
     *
     */
    internal fun enroll() {
        viewModel.handleEnrollment(getHeaderPayload(), getBodyPayload())
    }

    /**
     * Get view model instance
     */
    internal val getViewModelInstance: EnrollmentViewModel
        get() = viewModel


    init {
        val appContext: AppCompatActivity = context as AppCompatActivity
        viewModel = ViewModelProviders.of(
            appContext,
            CyberarkViewModelFactory(CyberarkAuthHelper(CyberarkAuthBuilder.cyberarkAuthService))
        ).get(EnrollmentViewModel::class.java)
    }

    /**
     * Get body payload
     *
     * @return
     */
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

    /**
     * Get header payload
     *
     * @return
     */
    private fun getHeaderPayload(): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_CENTRIFY_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_ACCEPT_LANGUAGE, "en-IN")
        payload.put(EndpointUrls.HEADER_AUTHORIZATION, "Bearer " + accessToken)
        return payload
    }
}