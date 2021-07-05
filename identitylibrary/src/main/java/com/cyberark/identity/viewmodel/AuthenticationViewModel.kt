package com.cyberark.identity.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val cyberarkAuthHelper: CyberarkAuthHelper) : ViewModel() {

    private val TAG: String? = AuthenticationViewModel::class.simpleName
    private val authResponse = MutableLiveData<ResponseHandler<AuthCodeFlowModel>>()

    init {
        Log.i("initialize", "initialize AuthenticationViewModel")
    }

    fun handleAuthorizationCode(params: HashMap<String?, String?>) {
        viewModelScope.launch {
            authResponse.postValue(ResponseHandler.loading(null))
            try {
                val accessCredentials = cyberarkAuthHelper.getAccessToken(params)
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "accessCredentials :: " + accessCredentials.toString())
                authResponse.postValue(ResponseHandler.success(accessCredentials))
            } catch (e: Exception) {
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "Exception :: " + e.toString())
                authResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    fun getAccessToken(): LiveData<ResponseHandler<AuthCodeFlowModel>> {
        return authResponse
    }
}