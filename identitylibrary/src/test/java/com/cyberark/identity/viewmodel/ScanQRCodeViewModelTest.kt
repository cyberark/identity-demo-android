package com.cyberark.identity.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.util.keystore.KeyStoreProvider
import junit.framework.TestCase
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox

@RunWith(PowerMockRunner::class)
@PrepareForTest(ScanQRCodeViewModel::class,CyberarkAuthHelper::class,QRCodeLoginModel::class)
class ScanQRCodeViewModelTest : TestCase() {
    private lateinit var cyberarkAuthHelper:CyberarkAuthHelper;
    private lateinit var scanQRCodeViewModel:ScanQRCodeViewModel
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @Mock
    private lateinit var observer: Observer<ResponseHandler<QRCodeLoginModel>>

    @Before
    public override fun setUp() {
        cyberarkAuthHelper = PowerMockito.mock(CyberarkAuthHelper::class.java)
        scanQRCodeViewModel = ScanQRCodeViewModel(cyberarkAuthHelper)
    }

    @Test
    public fun testHandleQRCodeResult() {
        val accessToken = getKeyStoreAccessToken()
        val qrModel = PowerMockito.mock(QRCodeLoginModel::class.java)

        scanQRCodeViewModel.qrCodeLogin().observeForever(observer)
            val payload = getHeaderPayload(accessToken!!)
        runBlocking {
                PowerMockito.`when`(cyberarkAuthHelper.qrCodeLogin(anyBoolean(), anyString(), anyString())).thenReturn(qrModel)
                scanQRCodeViewModel.handleQRCodeResult(payload, "qrCodeString")
                verify(observer, atLeastOnce()).onChanged(any())
        }
    }

    private fun getKeyStoreAccessToken(): String? {
        return "accessToken"
    }

    private fun getHeaderPayload(accessTokenData:String): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_AUTHORIZATION, "Bearer $accessTokenData")
        return payload
    }

}