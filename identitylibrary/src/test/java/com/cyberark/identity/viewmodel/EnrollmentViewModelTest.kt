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
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox

@RunWith(PowerMockRunner::class)
@PrepareForTest(EnrollmentViewModel::class, CyberArkAuthHelper::class,JSONObject::class,EnrollmentModel::class)
class EnrollmentViewModelTest : TestCase() {

    private lateinit var cyberArkAuthHelper:CyberArkAuthHelper
    private lateinit var enrollmentViewModel:EnrollmentViewModel

    @Mock
    private lateinit var enrollObserver: Observer<ResponseHandler<EnrollmentModel>>

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    public override fun setUp() {
        cyberArkAuthHelper = PowerMockito.mock(CyberArkAuthHelper::class.java)
        enrollmentViewModel = EnrollmentViewModel(cyberArkAuthHelper)
    }

    @Test
    fun testHandleEnrollment() {

        val acceptCode = "Code"
        val authorization = "Authrorization"
        val isIdaptive = true

        val jsonObject = JSONObject()
        jsonObject.put(EndpointUrls.HEADER_ACCEPT_LANGUAGE,acceptCode)
        jsonObject.put(EndpointUrls.HEADER_AUTHORIZATION,authorization)
        jsonObject.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT,isIdaptive)

        val requestBody = Whitebox.invokeMethod<RequestBody>(enrollmentViewModel,"createJsonBody",getBodyPayload().toString())

        runBlocking {
            PowerMockito.`when`(cyberArkAuthHelper.fastEnrollV3(isIdaptive,acceptCode,authorization,requestBody)).thenReturn( PowerMockito.mock(EnrollmentModel::class.java))
            enrollmentViewModel.getEnrolledData().observeForever(enrollObserver)
            enrollmentViewModel.handleEnrollment(jsonObject,getBodyPayload())
            verify(enrollObserver, atLeastOnce()).onChanged(any())
        }

    }

    private fun getBodyPayload(): JSONObject {
        val payload = JSONObject()
        payload.put("name", "Pixel 5 (IMEI: 3fcf45ed38044067)")
        payload.put("simpleName", "Pixel 5")
        payload.put("version", "11")
        payload.put("udid", "3fcf45ed38044067-3fcf45ed38044067-1560043795")
        payload.put("Manufacturer", "Google")
        payload.put("imei", "3fcf45ed38044067")
        payload.put("os", "Android")
        payload.put("GoogleServiceEnabled", true)
        return payload
    }
}