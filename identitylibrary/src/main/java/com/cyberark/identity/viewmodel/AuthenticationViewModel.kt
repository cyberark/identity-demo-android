package com.cyberark.identity.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val cyberarkAuthHelper: CyberarkAuthHelper) : ViewModel() {

    private val TAG: String? = AuthenticationViewModel::class.simpleName
    private val authResponse = MutableLiveData<ResponseHandler<AuthCodeFlowModel>>()
    private val refreshTokenResponse = MutableLiveData<ResponseHandler<RefreshTokenModel>>()

    init {
        Log.i(TAG, "initialize AuthenticationViewModel")
    }

    fun handleAuthorizationCode(params: HashMap<String?, String?>) {
        viewModelScope.launch {
            authResponse.postValue(ResponseHandler.loading(null))
            try {
                val accessTokenCreds = cyberarkAuthHelper.getAccessToken(params)
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "accessCredentials :: " + accessTokenCreds.toString())
                authResponse.postValue(ResponseHandler.success(accessTokenCreds))
            } catch (e: Exception) {
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "Exception :: " + e.toString())
                authResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    fun handleRefreshToken(params: HashMap<String?, String?>) {
        viewModelScope.launch {
            refreshTokenResponse.postValue(ResponseHandler.loading(null))
            try {
                val refreshTokenCreds = cyberarkAuthHelper.refreshToken(params)
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "refreshTokenCreds :: " + refreshTokenCreds.toString())
                refreshTokenResponse.postValue(ResponseHandler.success(refreshTokenCreds))
            } catch (e: Exception) {
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "Exception :: " + e.toString())
                refreshTokenResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    fun getAccessToken(): LiveData<ResponseHandler<AuthCodeFlowModel>> {
        return authResponse
    }

    fun getRefreshToken(): LiveData<ResponseHandler<RefreshTokenModel>> {
        return refreshTokenResponse
    }
}