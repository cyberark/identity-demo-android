/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyberark.identity.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
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

@RunWith(PowerMockRunner::class)
@PrepareForTest(ScanQRCodeViewModel::class,CyberArkAuthHelper::class,QRCodeLoginModel::class)
class ScanQRCodeViewModelTest : TestCase() {
    private lateinit var cyberArkAuthHelper:CyberArkAuthHelper
    private lateinit var scanQRCodeViewModel:ScanQRCodeViewModel
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @Mock
    private lateinit var observer: Observer<ResponseHandler<QRCodeLoginModel>>

    @Before
    public override fun setUp() {
        cyberArkAuthHelper = PowerMockito.mock(CyberArkAuthHelper::class.java)
        scanQRCodeViewModel = ScanQRCodeViewModel(cyberArkAuthHelper)
    }

    @Test
    fun testHandleQRCodeResult() {
        val accessToken = getKeyStoreAccessToken()
        val qrModel = PowerMockito.mock(QRCodeLoginModel::class.java)

        scanQRCodeViewModel.qrCodeLogin().observeForever(observer)
        val payload = getHeaderPayload(accessToken)
        runBlocking {
            PowerMockito.`when`(cyberArkAuthHelper.qrCodeLogin(anyBoolean(), anyString(), anyString())).thenReturn(qrModel)
            scanQRCodeViewModel.handleQRCodeResult(payload, "qrCodeString")
            verify(observer, atLeastOnce()).onChanged(any())
        }
    }

    private fun getKeyStoreAccessToken(): String {
        return "accessToken"
    }

    private fun getHeaderPayload(accessTokenData:String): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_AUTHORIZATION, "Bearer $accessTokenData")
        return payload
    }

}