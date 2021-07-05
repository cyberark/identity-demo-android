package com.cyberark.identity.data.network

import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.util.EndpointUrls.URL_AUTH_CODE_FLOW
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface CyberarkAuthService {

    @POST
    suspend fun qrCodeLogin(@Url url: String): QRCodeLoginModel

    @FormUrlEncoded
    @POST(URL_AUTH_CODE_FLOW)
    suspend fun getAccessToken(@FieldMap params: HashMap<String?, String?>): AuthCodeFlowModel

}