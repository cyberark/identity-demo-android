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
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(ScanQRCodeViewModel::class,CyberArkAuthHelper::class)
class AuthenticationViewModelTest : TestCase() {
    private lateinit var cyberArkAuthHelper:CyberArkAuthHelper
    private lateinit var authenticationViewModel:AuthenticationViewModel
    @Mock
    private lateinit var authObserver: Observer<ResponseHandler<AuthCodeFlowModel>>
    @Mock
    private lateinit var refreshObserver: Observer<ResponseHandler<RefreshTokenModel>>
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    public override fun setUp() {
        cyberArkAuthHelper = PowerMockito.mock(CyberArkAuthHelper::class.java)
        authenticationViewModel = AuthenticationViewModel(cyberArkAuthHelper)
    }

    @Test
    fun testHandleAuthorizationCode() {
        val params = HashMap<String?, String?>()
        authenticationViewModel.getAccessToken().observeForever(authObserver)
        authenticationViewModel.handleAuthorizationCode(params,"")
        verify(authObserver, atLeastOnce()).onChanged(any())
    }

    @Test
    fun testHandleRefreshToken() {
        val params = HashMap<String?, String?>()
        authenticationViewModel.getRefreshToken().observeForever(refreshObserver)
        authenticationViewModel.handleRefreshToken(params,"")
        verify(refreshObserver, atLeastOnce()).onChanged(any())
    }
}