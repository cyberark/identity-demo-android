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

package com.cyberark.identity.provider.manager

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.net.UrlQuerySanitizer
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelStore
import com.cyberark.identity.testUtility.Constants
import com.cyberark.identity.activity.CyberArkAuthActivity
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.base.CyberArkViewModelFactory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox

@RunWith(PowerMockRunner::class)
@PrepareForTest(
    AuthenticationViewModel::class,
    CyberArkAccountBuilder::class,
    AuthenticationViewModel::class,
    CyberArkViewModelFactory::class,
    ViewModelStore::class,
    CyberArkAuthActivity::class,
    Uri::class,
    UrlQuerySanitizer::class,
    CyberArkAuthManager::class
)
@PowerMockIgnore("javax.net.ssl.*")
class CyberArkAuthManagerTest {

    @Mock
    lateinit var appCompactActivty:AppCompatActivity
    @Mock
    internal lateinit var authenticationViewModel: AuthenticationViewModel
    @Mock
    internal lateinit var cyberArkBuilder: CyberArkAccountBuilder

    private lateinit var authManager: CyberArkAuthManager

    @Before
    fun setUp() {
        val application = PowerMockito.mock(Application::class.java)
        val viewModelStore = PowerMockito.mock(ViewModelStore::class.java)
        PowerMockito.`when`(appCompactActivty.application).thenReturn(application)
        PowerMockito.`when`(appCompactActivty.viewModelStore).thenReturn(viewModelStore)
        PowerMockito.`when`(cyberArkBuilder.getBaseSystemUrl).thenReturn(Constants.systemURL)
        PowerMockito.`when`(cyberArkBuilder.getBaseUrl).thenReturn(Constants.appendHTTPs(Constants.domainURL))
         authManager = CyberArkAuthManager(appCompactActivty,cyberArkBuilder)
        Whitebox.setInternalState(authManager,"viewModel",authenticationViewModel)
    }

    @Test
    fun endSession() {
        val mockUri = PowerMockito.mock(Uri::class.java)
        PowerMockito.mockStatic(CyberArkAuthActivity::class.java)
        PowerMockito.`when`(cyberArkBuilder.getOAuthEndSessionURL).thenReturn("https://tenant.com/endsession")
        PowerMockito.mockStatic(Uri::class.java)
        PowerMockito.`when`(Uri.parse(anyString())).thenReturn(mockUri)
        Whitebox.setInternalState(authManager,"viewModel",authenticationViewModel)
        authManager.endSession()
    }

    @Test
    fun startAuthentication() {
        val mockUri = PowerMockito.mock(Uri::class.java)
        PowerMockito.mockStatic(CyberArkAuthActivity::class.java)
        PowerMockito.`when`(cyberArkBuilder.getOAuthBaseURL).thenReturn("https://tenant.com/endsession")
        PowerMockito.mockStatic(Uri::class.java)
        PowerMockito.`when`(Uri.parse(anyString())).thenReturn(mockUri)
        Whitebox.setInternalState(authManager,"viewModel",authenticationViewModel)
        authManager.startAuthentication()
    }

    @Test
    fun updateResult() {
        val intent = PowerMockito.mock(Intent::class.java)
        val mockUri = PowerMockito.mock(Uri::class.java)
        PowerMockito.`when`(mockUri.getQueryParameter(CyberArkAccountBuilder.KEY_CODE)).thenReturn("code")
        PowerMockito.`when`(intent.data).thenReturn(mockUri)
        val mockSanitizer: UrlQuerySanitizer = PowerMockito.mock(UrlQuerySanitizer::class.java)
        PowerMockito.whenNew(UrlQuerySanitizer::class.java).withNoArguments().thenReturn(mockSanitizer)
        PowerMockito.`when`(mockSanitizer.getValue(CyberArkAccountBuilder.KEY_CODE)).thenReturn("code")

        authManager.updateResultForAccessToken(intent)

        verify(authenticationViewModel).handleAuthorizationCode(
            anyOrNull(),
            anyOrNull()
        )
        PowerMockito.`when`(mockSanitizer.getValue(CyberArkAccountBuilder.KEY_CODE)).thenReturn(null)
//        PowerMockito.`when`(intent.data).thenReturn(null)
        authManager.updateResultForAccessToken(intent)
        verify(authenticationViewModel).handleAuthorizationCode(anyOrNull(), anyOrNull())
    }

    @Test
    fun refreshToken() {
        val refreshToken = "refreshToken"
        authManager.refreshToken(refreshToken)
        verify(authenticationViewModel).handleRefreshToken(anyOrNull(), anyOrNull())
    }


}