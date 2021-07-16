package com.cyberark.identity.provider

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.cyberark.identity.data.network.CyberarkAuthBuilder
import com.cyberark.identity.data.network.CyberarkAuthHelper
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

    //TODO.. need to remove all hardcoded values
    private fun getBodyPayload(): JSONObject {
        val payload = JSONObject()
        payload.put("devicetype", "A")
        payload.put("name", "Pixel 5 (IMEI: 3fcf45ed38044067)")
        payload.put("simpleName", "Pixel 5")
        payload.put("version", "11")
        payload.put("udid", "3fcf45ed38044067-3fcf45ed38044067-1560043795")
        payload.put("Manufacturer", "Google")
        payload.put("imei", "3fcf45ed38044067")
        payload.put("os", "Android")
        payload.put("GoogleServiceEnabled", true)
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