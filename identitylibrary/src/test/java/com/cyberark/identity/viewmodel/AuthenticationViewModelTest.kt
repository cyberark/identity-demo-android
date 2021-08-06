package com.cyberark.identity.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.data.network.CyberarkAuthHelper
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
@PrepareForTest(ScanQRCodeViewModel::class,CyberarkAuthHelper::class)
class AuthenticationViewModelTest : TestCase() {
    private lateinit var cyberarkAuthHelper:CyberarkAuthHelper;
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
        cyberarkAuthHelper = PowerMockito.mock(CyberarkAuthHelper::class.java)
        authenticationViewModel = AuthenticationViewModel(cyberarkAuthHelper)
    }

    @Test
    public fun testHandleAuthorizationCode() {
        val params = HashMap<String?, String?>()
        authenticationViewModel.getAccessToken().observeForever(authObserver)
        authenticationViewModel.handleAuthorizationCode(params)
        verify(authObserver, atLeastOnce()).onChanged(any())
    }

    @Test
    public fun testHandleRefreshToken() {
        val params = HashMap<String?, String?>()
        authenticationViewModel.getRefreshToken().observeForever(refreshObserver)
        authenticationViewModel.handleRefreshToken(params)
        verify(refreshObserver, atLeastOnce()).onChanged(any())
    }
}