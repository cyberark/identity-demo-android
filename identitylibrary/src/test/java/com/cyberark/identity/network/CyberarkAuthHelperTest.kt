package com.cyberark.identity.data.network

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(CyberArkAuthService::class)
class CyberarkAuthHelperTest : TestCase() {

    @Mock
    lateinit var cyberarkAuthService: CyberArkAuthService

    lateinit var cyberarkAuthHelper:CyberArkAuthHelper

    @Before
    public override fun setUp() {
        super.setUp()
        cyberarkAuthHelper = CyberArkAuthHelper(cyberarkAuthService)
    }

    @Test
    public fun testqrCodeLogin() {
        var dapNativeClient = true
        var bearerToken = "mockToken"
        var urlString = "urlString"
        runBlocking {
            cyberarkAuthHelper.qrCodeLogin(dapNativeClient,bearerToken,urlString)
            verify(cyberarkAuthService).qrCodeLogin(dapNativeClient,bearerToken,urlString)
        }
    }

    @Test
    public fun testgetAccessToken() {
        val hashMap = PowerMockito.mock(HashMap::class.java)
        runBlocking {
            cyberarkAuthHelper.getAccessToken(hashMap as HashMap<String?, String?>,"")
            verify(cyberarkAuthService).getAccessToken(hashMap as HashMap<String?, String?>,"")
        }
    }

    @Test
    public fun testrefreshToken() {
        val hashMap = PowerMockito.mock(HashMap::class.java)
        runBlocking {
            cyberarkAuthHelper.refreshToken(hashMap as HashMap<String?, String?>,"")
            verify(cyberarkAuthService).refreshToken(hashMap as HashMap<String?, String?>,"")
        }
    }

    @Test
    public fun testfastEnrollV3() {
        runBlocking {
            var centrifyNativeClient: Boolean = true
            var idapNativeClient: Boolean =  true
            var acceptLang: String = "acceptLang"
            var bearerToken: String = "bearerToken"
            var requestBody = PowerMockito.mock(RequestBody::class.java)
            cyberarkAuthHelper.fastEnrollV3(centrifyNativeClient,idapNativeClient,acceptLang,bearerToken,requestBody)
            verify(cyberarkAuthService).fastEnrollV3(centrifyNativeClient,idapNativeClient,acceptLang,bearerToken,requestBody)
        }
    }

}