package com.cyberark.identity.provider

import android.app.Application
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.provider.manager.CyberArkAuthManager
import com.cyberark.identity.provider.manager.CyberArkEnrollmentManager
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.powermock.api.mockito.PowerMockito
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.whenNew
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(
    CyberArkAuthProvider::class,
    CyberArkAccountBuilder::class,
    CyberArkAuthManager::class,
    AuthenticationViewModel::class,
    Uri::class,
    CyberArkEnrollmentManager::class,
    EnrollmentViewModel::class
)
@PowerMockIgnore("javax.net.ssl.*")
class CyberArkAuthProviderTest {

    @Mock
    internal lateinit var appCompactActivty: AppCompatActivity

    @Mock
    internal lateinit var authenticationViewModel: AuthenticationViewModel


    @Before
    fun setUp() {
        val application = mock(Application::class.java)
        val viewModelStore = mock(ViewModelStore::class.java)
        val viewModelProvider = mock(ViewModelProvider::class.java)
        whenNew(ViewModelProvider::class.java).withAnyArguments()
            .thenReturn(viewModelProvider)
        PowerMockito.`when`(viewModelProvider.get(AuthenticationViewModel::class.java))
            .thenReturn(authenticationViewModel)
        PowerMockito.`when`(appCompactActivty.application).thenReturn(application)
        PowerMockito.`when`(appCompactActivty.viewModelStore).thenReturn(viewModelStore)
    }

    @Test
    fun login() {
        val accountBuilder = mock(CyberArkAccountBuilder::class.java)
        val loginBuilder = CyberArkAuthProvider.login(accountBuilder)
        PowerMockito.`when`(accountBuilder.getBaseUrl).thenReturn("https://tenant.com/endsession/")
        PowerMockito.`when`(accountBuilder.getOAuthBaseURL).thenReturn("https://tenant.com/endsession/")
        PowerMockito.mockStatic(Uri::class.java)
        val mockUri = mock(Uri::class.java)
        PowerMockito.`when`(Uri.parse(Mockito.anyString())).thenReturn(mockUri)
        loginBuilder.start(appCompactActivty)
        verify(authenticationViewModel).getAccessToken()
    }

    @Test
    fun endSession() {
        val accountBuilder = mock(CyberArkAccountBuilder::class.java)
        val endSessionBuilder = CyberArkAuthProvider.endSession(accountBuilder)
        val authManager = mock(CyberArkAuthManager::class.java)
        whenNew(CyberArkAuthManager::class.java).withArguments(appCompactActivty,accountBuilder).thenReturn(authManager)

        endSessionBuilder.start(appCompactActivty)
        verify(authManager).endSession()
    }

    @Test
    fun refreshToken() {
        val refreshToken = "sampleRefreshToken"
        val accountBuilder = mock(CyberArkAccountBuilder::class.java)
        val refreshBuilder = CyberArkAuthProvider.refreshToken(accountBuilder)
        val authManager = mock(CyberArkAuthManager::class.java)
        whenNew(CyberArkAuthManager::class.java).withArguments(appCompactActivty,accountBuilder).thenReturn(authManager)
        PowerMockito.`when`(authManager.getViewModelInstance).thenReturn(authenticationViewModel)
        refreshBuilder.start(appCompactActivty,refreshToken)
        verify(authenticationViewModel).getRefreshToken()
    }

    @Test
    fun enroll() {
        val accessToken = "sampleAccessToken"
        val enrollmentManager = mock(CyberArkEnrollmentManager::class.java)
        val enrollmentViewModel = mock(EnrollmentViewModel::class.java)
        val accountBuilder = mock(CyberArkAccountBuilder::class.java)
        whenNew(CyberArkEnrollmentManager::class.java).withArguments(appCompactActivty,accessToken,accountBuilder).thenReturn(enrollmentManager)
        PowerMockito.`when`(enrollmentManager.getViewModelInstance).thenReturn(enrollmentViewModel)
        val enrollmentBuilder = CyberArkAuthProvider.enroll(accountBuilder)
        enrollmentBuilder.start(appCompactActivty,accessToken)
        verify(enrollmentManager).enroll()
    }
}