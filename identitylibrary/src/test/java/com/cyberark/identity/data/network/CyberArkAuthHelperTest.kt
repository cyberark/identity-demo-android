package com.cyberark.identity.data.network

import com.cyberark.identity.builder.CyberArkAccountBuilder
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
class CyberArkAuthHelperTest : TestCase() {

    @Mock
    lateinit var cyberArkAuthService: CyberArkAuthService

    private lateinit var cyberArkAuthHelper: CyberArkAuthHelper

    @Before
    public override fun setUp() {
        super.setUp()
        cyberArkAuthHelper = CyberArkAuthHelper(cyberArkAuthService)
    }

    @Test
    fun testQrCodeLogin() {
        val idapNativeClient = true
        val bearerToken = "mockToken"
        val qrCodeUrl = "mockQrCodeUrl"

        runBlocking {
            cyberArkAuthHelper.qrCodeLogin(idapNativeClient, bearerToken, qrCodeUrl)
            verify(cyberArkAuthService).qrCodeLogin(idapNativeClient, bearerToken, qrCodeUrl)
        }
    }

    @Test
    fun testGetAccessToken() {
        val params = HashMap<String?, String?>()
        params[CyberArkAccountBuilder.KEY_GRANT_TYPE] =
            "mockGrantType"
        params[CyberArkAccountBuilder.KEY_CODE] = "mockCode"
        params[CyberArkAccountBuilder.KEY_REDIRECT_URI] = "mockRedirectUri"
        params[CyberArkAccountBuilder.KEY_CLIENT_ID] = "mockClientId"
        params[CyberArkAccountBuilder.KEY_CODE_VERIFIER] = "mockCodeVerifier"
        params[CyberArkAccountBuilder.KEY_STATE] = "mockState"

        val tokenURL = "mockTokenUrl"

        runBlocking {
            cyberArkAuthHelper.getAccessToken(params, tokenURL)
            verify(cyberArkAuthService).getAccessToken(params, tokenURL)
        }
    }

    @Test
    fun testRefreshToken() {
        val params = HashMap<String?, String?>()
        params[CyberArkAccountBuilder.KEY_CLIENT_ID] = "mockClientId"
        params[CyberArkAccountBuilder.KEY_GRANT_TYPE] = "mockGrantType"
        params[CyberArkAccountBuilder.KEY_REFRESH_TOKEN] = "mockRefreshToken"

        val tokenURL = "mockTokenUrl"

        runBlocking {
            cyberArkAuthHelper.refreshToken(params, tokenURL)
            verify(cyberArkAuthService).refreshToken(params, tokenURL)
        }
    }

    @Test
    fun testGetUserInfo() {
        val idapNativeClient = true
        val bearerToken = "mockToken"
        val userInfoUrl = "mockUserInfoUrl"

        runBlocking {
            cyberArkAuthHelper.getUserInfo(idapNativeClient, bearerToken, userInfoUrl)
            verify(cyberArkAuthService).getUserInfo(idapNativeClient, bearerToken, userInfoUrl)
        }
    }

    @Test
    fun testFastEnrollV3() {
        val idapNativeClient = true
        val acceptLang = "acceptLang"
        val bearerToken = "bearerToken"
        val requestBody = PowerMockito.mock(RequestBody::class.java)

        runBlocking {
            cyberArkAuthHelper.fastEnrollV3(idapNativeClient, acceptLang, bearerToken, requestBody)
            verify(cyberArkAuthService).fastEnrollV3(
                idapNativeClient,
                acceptLang,
                bearerToken,
                requestBody
            )
        }
    }

    @Test
    fun testSendFCMToken() {
        val idapNativeClient = true
        val bearerToken = "mockBearerToken"
        val requestBody = PowerMockito.mock(RequestBody::class.java)

        runBlocking {
            cyberArkAuthHelper.sendFCMToken(idapNativeClient, bearerToken, requestBody)
            verify(cyberArkAuthService).sendFCMToken(idapNativeClient, bearerToken, requestBody)
        }
    }

    @Test
    fun testOtpEnroll() {
        val bearerToken = "mockBearerToken"
        val otpEnrollUrl = "mockOTPEnrollUrl"

        runBlocking {
            cyberArkAuthHelper.otpEnroll(bearerToken, otpEnrollUrl)
            verify(cyberArkAuthService).otpEnroll(bearerToken, otpEnrollUrl)
        }
    }

    @Test
    fun testSubmitOTPCode() {
        val bearerToken = "mockBearerToken"
        val otpCode = "mockOtpCode"
        val otpKeyVersion = 10
        val otpTimeStamp: Long = 3256784
        val userAccepted = true
        val otpExpiryInterval = 60
        val otpChallengeAnswer = "mockOtpChallenge"
        val udid = "mockUuid"

        runBlocking {
            cyberArkAuthHelper.submitOTPCode(
                bearerToken,
                otpCode,
                otpKeyVersion,
                otpTimeStamp,
                userAccepted,
                otpExpiryInterval,
                otpChallengeAnswer,
                udid
            )
            verify(cyberArkAuthService).submitOTPCode(
                bearerToken,
                otpCode,
                otpKeyVersion,
                otpTimeStamp,
                userAccepted,
                otpExpiryInterval,
                otpChallengeAnswer,
                udid
            )
        }
    }

    @Test
    fun testSignupWithCaptcha() {
        val idapNativeClient = true
        val requestBody = PowerMockito.mock(RequestBody::class.java)

        runBlocking {
            cyberArkAuthHelper.signupWithCaptcha(idapNativeClient, requestBody)
            verify(cyberArkAuthService).signupWithCaptcha(idapNativeClient, requestBody)
        }
    }
}