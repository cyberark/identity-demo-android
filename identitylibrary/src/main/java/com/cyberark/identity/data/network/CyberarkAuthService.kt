package com.cyberark.identity.data.network

import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_AUTH_CODE_FLOW
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_FAST_ENROLL_V3
import okhttp3.RequestBody
import retrofit2.http.*

interface CyberarkAuthService {

    @POST
    suspend fun qrCodeLogin(@Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
                            @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
                            @Url url: String): QRCodeLoginModel

    @FormUrlEncoded
    @POST(URL_AUTH_CODE_FLOW)
    suspend fun getAccessToken(@FieldMap params: HashMap<String?, String?>): AuthCodeFlowModel

    @FormUrlEncoded
    @POST(URL_AUTH_CODE_FLOW)
    suspend fun refreshToken(@FieldMap params: HashMap<String?, String?>): RefreshTokenModel

    @POST(URL_FAST_ENROLL_V3)
    suspend fun fastEnrollV3(@Header(EndpointUrls.HEADER_X_CENTRIFY_NATIVE_CLIENT) centrifyNativeClient: Boolean,
                             @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
                             @Header(EndpointUrls.HEADER_ACCEPT_LANGUAGE) acceptLang: String,
                             @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
                             @Body payload: RequestBody): EnrollmentModel

}