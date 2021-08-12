package com.cyberark.identity.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
import kotlinx.coroutines.launch
import org.json.JSONObject

internal class ScanQRCodeViewModel(private val cyberarkAuthHelper: CyberarkAuthHelper) : ViewModel() {

    private val TAG: String? = ScanQRCodeViewModel::class.simpleName
    private val qrCodeResponse = MutableLiveData<ResponseHandler<QRCodeLoginModel>>()

    init {
        Log.i(TAG, "initialize ScanQRCodeViewModel")
    }

    internal fun handleQRCodeResult(headerPayload: JSONObject, barcodeUrl: String) {
        viewModelScope.launch {
            qrCodeResponse.postValue(ResponseHandler.loading(null))
            try {
                Log.i(TAG, headerPayload.toString())
                Log.i(TAG, barcodeUrl)
                val idapNativeClient: Boolean = headerPayload.getBoolean(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT)
                val bearerToken: String = headerPayload.getString(EndpointUrls.HEADER_AUTHORIZATION)
                val usersFromApi = cyberarkAuthHelper.qrCodeLogin(idapNativeClient, bearerToken, barcodeUrl)
                Log.i(TAG, "usersFromApi :: "+ usersFromApi.success)
                Log.i(TAG, "usersFromApi :: "+ usersFromApi.result?.displayName)
                Log.i(TAG, "usersFromApi String :: "+ usersFromApi.toString())
                qrCodeResponse.postValue(ResponseHandler.success(usersFromApi))
            } catch (e: Exception) {
                Log.i(TAG, "usersFromApi Error :: "+ e.toString())
                qrCodeResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    internal fun qrCodeLogin(): LiveData<ResponseHandler<QRCodeLoginModel>> {
        return qrCodeResponse
    }
}