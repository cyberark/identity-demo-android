package com.cyberark.mfa.ui.qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import kotlinx.coroutines.launch

class ScanQRCodeViewModel(private val cyberarkAuthHelper: CyberarkAuthHelper) : ViewModel() {

    private val qrCodeResponse = MutableLiveData<ResponseHandler<QRCodeLoginModel>>()

    init {
        validateQRCodeResult()
    }

    private fun validateQRCodeResult() {
        viewModelScope.launch {
            qrCodeResponse.postValue(ResponseHandler.loading(null))
            try {
                val usersFromApi = cyberarkAuthHelper.qrCodeLogin("barCodeResult")
                qrCodeResponse.postValue(ResponseHandler.success(usersFromApi))
            } catch (e: Exception) {
                qrCodeResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    fun qrCodeLogin(): LiveData<ResponseHandler<QRCodeLoginModel>> {
        return qrCodeResponse
    }

}