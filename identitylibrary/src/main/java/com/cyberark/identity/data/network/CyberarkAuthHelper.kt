package com.cyberark.identity.data.network

import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.RefreshTokenModel

class CyberarkAuthHelper(private val cyberarkAuthService: CyberarkAuthService) {

    suspend fun qrCodeLogin(url: String) = cyberarkAuthService.qrCodeLogin(url)

    suspend fun getAccessToken(params: HashMap<String?,
            String?>): AuthCodeFlowModel = cyberarkAuthService.getAccessToken(params)

    suspend fun refreshToken(params: HashMap<String?,
            String?>): RefreshTokenModel = cyberarkAuthService.refreshToken(params)

}