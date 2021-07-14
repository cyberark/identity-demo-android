package com.cyberark.identity.data.network

import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.model.RefreshTokenModel

class CyberarkAuthHelper(private val cyberarkAuthService: CyberarkAuthService) {

    suspend fun qrCodeLogin(dapNativeClient: Boolean,
                            bearerToken: String,
                            url: String): QRCodeLoginModel = cyberarkAuthService.qrCodeLogin(dapNativeClient, bearerToken, url)

    suspend fun getAccessToken(params: HashMap<String?,
            String?>): AuthCodeFlowModel = cyberarkAuthService.getAccessToken(params)

    suspend fun refreshToken(params: HashMap<String?,
            String?>): RefreshTokenModel = cyberarkAuthService.refreshToken(params)

}