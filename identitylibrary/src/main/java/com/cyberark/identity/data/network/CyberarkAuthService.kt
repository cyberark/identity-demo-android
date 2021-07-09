package com.cyberark.identity.data.network

import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_AUTH_CODE_FLOW
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface CyberarkAuthService {

    @POST
    suspend fun qrCodeLogin(@Url url: String): QRCodeLoginModel

    @FormUrlEncoded
    @POST(URL_AUTH_CODE_FLOW)
    suspend fun getAccessToken(@FieldMap params: HashMap<String?, String?>): AuthCodeFlowModel

    @FormUrlEncoded
    @POST(URL_AUTH_CODE_FLOW)
    suspend fun refreshToken(@FieldMap params: HashMap<String?, String?>): RefreshTokenModel

}